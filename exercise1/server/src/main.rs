use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream, UdpSocket};
use std::sync::{Arc, Mutex, mpsc, mpsc::TrySendError::{Full}};
use std::time::Duration;
use std::process::exit;
use std::thread;
use std::thread::sleep;
use server::thread_pool::ThreadPool;
use ctrlc;


const MAX_NO_THREADS: usize = 8;
const LISTENER_ADDRESS: &str = "127.0.0.1:1234";

type Clients = Arc<Mutex<Vec<Client>>>;

struct Client {
    sender: mpsc::Sender<String>,
    id: usize,
}

fn main() {
    let listener = TcpListener::bind(LISTENER_ADDRESS).unwrap();
    let udp_listener = UdpSocket::bind(LISTENER_ADDRESS).unwrap();

    let pool = ThreadPool::new(MAX_NO_THREADS);

    // Creates vector of all clients
    let all_clients: Clients = Arc::new(Mutex::new(vec![]));
    let mut id: usize = 0;

    let all_clients_clone = Arc::clone(&all_clients);
    ctrlc::set_handler(move || {
        println!("Server closing...");
        // Notify all clients that server is closing
        for client in all_clients_clone.lock().unwrap().iter() {
            client.sender.send("QUIT".to_string()).unwrap()
        }
        sleep(Duration::from_millis(1000));
        exit(0);
    }).unwrap();

    let all_clients_clone = Arc::clone(&all_clients);
    thread::spawn(|| handle_udp(udp_listener, all_clients_clone));

    for stream in listener.incoming() {
        let stream = Arc::new(Mutex::new(stream.unwrap()));

        // Creates counting smart pointers to move to the thread
        let stream_clone = Arc::clone(&stream);
        let all_clients_clone = Arc::clone(&all_clients);

        // Creates channel for communication between threads
        let (sender, receiver) = mpsc::channel();

        // Spawn thread and handle errors
        match pool.execute(move || handle_client(Arc::clone(&stream_clone), id, all_clients_clone, receiver)) {
            Err(Full(_)) => {
                stream.lock().unwrap().write_all("Err: No available threads in pool.".as_bytes()).unwrap();
            }
            Err(_disconnected) => {
                stream.lock().unwrap().write_all("Err: Server is already closing.".as_bytes()).unwrap();
            }
            Ok(_) => {
                stream.lock().unwrap().write_all(id.to_string().as_bytes()).unwrap();
                // Sets max wait time to read
                stream.lock().unwrap().set_read_timeout(Some(Duration::from_millis(10))).unwrap();
                all_clients.lock().unwrap().push(Client { sender, id });
                id += 1;
            }
        }
    }
}

fn handle_client(stream: Arc<Mutex<TcpStream>>, id: usize, clients: Clients, receiver: mpsc::Receiver<String>) {
    loop {
        let mut stream = stream.lock().unwrap();

        // Check for messages from other clients
        if let Ok(message) = receiver.try_recv() {
            if let Err(_) = stream.write_all(message.as_bytes()) {
                println!("Connection has been closed");
            }
        }

        // Read from client
        let mut buffer = [0; 1024];
        if let Ok(n) = stream.read(&mut buffer) {
            if n > 0 && n < 1024 {
                let message = String::from_utf8_lossy(&buffer[..n]);
                if message.to_uppercase().eq("QUIT") {
                    // Handle QUIT message
                    println!("Closing client {id}");
                    let mut clients = clients.lock().unwrap();
                    for (i, client) in clients.iter().enumerate() {
                        if client.id == id {
                            clients.remove(i);
                            break;
                        }
                    }
                    break;
                } else {
                    // Forward message to other clients
                    let message_to_send = format!("Message from {}: {}", id, message);
                    for client in clients.lock().unwrap().iter() {
                        if client.id != id {
                            match client.sender.send(String::clone(&message_to_send)) {
                                _ => (),
                            }
                        }
                    }
                }
            }
        }
    }
}

fn handle_udp(udp_socket: UdpSocket, all_clients: Clients) {
    loop {
        let mut buffer = [0; 1024];
        // read UDP packets
        if let Ok((n, addr)) = udp_socket.recv_from(&mut buffer) {
            if n > 0 && n < 1024 {
                // find out who sent the packet
                let id: usize = (addr.port() - 50000).into();
                // Format message
                let message = String::from_utf8_lossy(&buffer[..n]);
                let message = format!("Message from {id}:\n{}", message);
                // Forward message to other clients
                for client in all_clients.lock().unwrap().iter() {
                    if client.id != id {
                        let address = format!("127.0.0.1:{}", 50000 + client.id);
                        if let Err(_) = udp_socket.send_to(message.as_bytes(), address) {
                            println!("Couldn't send data to client {}.", client.id);
                        }
                    }
                }
            }
        }
    }
}

