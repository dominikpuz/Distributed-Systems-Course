use std::io::{Read, Write};
use std::net::{TcpStream, UdpSocket};
use std::io::stdin;
use std::sync::{Arc, Mutex};
use std::sync::atomic::{AtomicBool, Ordering};
use std::thread;
use std::thread::sleep;
use std::time::Duration;

const SERVER_ADDRESS: &str = "127.0.0.1:1234";
const ASCII_ART: &str = "   _______\n  /\\ o o o\\\n /o \\ o o o\\_______\n<    >------>   o /|\n \\ o/  o   /_____/o|\n  \\/______/     |oo|\n        |   o   |o/\n        |_______|/";

fn main() {
    let stream = TcpStream::connect(SERVER_ADDRESS);
    if let Err(_) = stream {
        println!("Couldn't connect");
        return;
    }
    let mut buffer = [0; 1024];
    let mut stream = stream.unwrap();
    // Read initial message to find out if connection was correctly established
    let n = stream.read(&mut buffer).unwrap();
    let initial_message = String::from_utf8_lossy(&buffer[..n]);
    let id: usize;
    if initial_message.starts_with("Err") {
        println!("{}", initial_message);
        return;
    } else {
        id = initial_message.parse().unwrap();
        println!("Your id: {}", id);
    }
    // Set up UDP socket
    let port = 50000 + id;
    let address = format!("127.0.0.1:{port}");
    let udp_socket = UdpSocket::bind(address).unwrap();
    udp_socket.set_read_timeout(Some(Duration::from_millis(10))).unwrap();

    let udp_socket = Arc::new(Mutex::new(udp_socket));
    let udp_clone = Arc::clone(&udp_socket);
    // Set max wait time to read
    stream.set_read_timeout(Some(Duration::from_millis(10))).unwrap();
    let closing = Arc::new(AtomicBool::new(false));
    let closing_clone = Arc::clone(&closing);

    let stream_reading = Arc::new(Mutex::new(stream));
    let stream_writing = Arc::clone(&stream_reading);
    let reading_thread = thread::spawn(|| handle_reading(stream_reading, closing, udp_socket));
    let writing_thread = thread::spawn(|| handle_writing(stream_writing, closing_clone, udp_clone));

    writing_thread.join().unwrap();
    reading_thread.join().unwrap();
}

fn handle_writing(stream: Arc<Mutex<TcpStream>>, closing: Arc<AtomicBool>, udp_socket: Arc<Mutex<UdpSocket>>) {
    while !closing.load(Ordering::Relaxed) {
        let mut message_to_send: String = Default::default();

        // Input message to send
        stdin().read_line(&mut message_to_send).unwrap();
        let message_to_send = message_to_send.trim();
        if message_to_send.eq("U") {
            if let Err(_) = udp_socket.lock().unwrap().send_to(ASCII_ART.as_bytes(), SERVER_ADDRESS) {
                println!("Couldn't send data.");
            }
        } else {
            // Send message
            if let Err(_) = stream.lock().unwrap().write_all(&message_to_send.as_bytes()) {
                println!("Connection has been closed");
            }

            // Handle QUIT message
            if message_to_send.to_uppercase().eq("QUIT") {
                println!("Closing");
                closing.store(true, Ordering::Relaxed);
                break;
            }
        }
    }
}

fn handle_reading(stream: Arc<Mutex<TcpStream>>, closing: Arc<AtomicBool>, udp_socket: Arc<Mutex<UdpSocket>>) {
    while !closing.load(Ordering::Relaxed) {
        let mut buffer = [0; 1024];
        // Check for UDP packets
        if let Ok((n, _)) = udp_socket.lock().unwrap().recv_from(&mut buffer) {
            if n > 0 && n < 1024 {
                let message = String::from_utf8_lossy(&buffer[..n]);
                println!("{message}");
                buffer = [0;1024];
            }
        }

        // Try to read incoming messages
        if let Ok(n) = stream.lock().unwrap().read(&mut buffer) {
            if n > 0 && n < 1024 {
                let message = String::from_utf8_lossy(&buffer[..n]);
                let message = message.trim();
                // Handle server closing
                if message.to_uppercase().eq("QUIT") {
                    closing.store(true, Ordering::Relaxed);
                    println!("Closing");
                    break;
                } else {
                    println!("{message}");
                }
            }
        }
        sleep(Duration::from_millis(100));
    }
}
