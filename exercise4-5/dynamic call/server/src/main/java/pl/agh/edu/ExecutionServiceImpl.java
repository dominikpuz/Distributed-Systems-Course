package pl.agh.edu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pl.agh.edu.gen.ExecutionRequest;
import pl.agh.edu.gen.ExecutionResponse;
import pl.agh.edu.gen.ExecutionServiceGrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecutionServiceImpl extends ExecutionServiceGrpc.ExecutionServiceImplBase {
    @Override
    public void execute(ExecutionRequest request, StreamObserver<ExecutionResponse> responseObserver) {
        System.out.println(request);
        ExecutionResponse.Builder responseBuilder = ExecutionResponse.newBuilder();
        try {
            ClassLoader classLoader = ExampleMethods.class.getClassLoader();
            Path jarPath = Paths.get(request.getJarLocation());
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, classLoader);
            String[] tempArray = request.getMethod().split(":");
            Class executableClass = urlClassLoader.loadClass(tempArray[0]);
            Object obj = executableClass.newInstance();
            String methodName = tempArray[1];
            Method[] methods = executableClass.getDeclaredMethods();
            boolean foundMethod = false;
            Method method;
            Object returnedObject = null;
            Gson gson = new GsonBuilder().setLenient().create();
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    foundMethod = true;
                    Class[] pTypes = m.getParameterTypes();
                    if (pTypes.length == 1) {
                        Class pType = pTypes[0];
                        method = executableClass.getMethod(methodName, pType);
                        System.out.println(pType);
                        System.out.println(gson.fromJson(request.getData(), pType));
                        returnedObject = method.invoke(obj, gson.fromJson(request.getData(), pType));
                    } else if (pTypes.length == 0) {
                        method = executableClass.getMethod(methodName);
                        returnedObject = method.invoke(obj);
                    }
                    break;
                }
            }
            if (foundMethod) {
                if (returnedObject != null) {
                    responseBuilder.setData(gson.toJson(returnedObject));
                }
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not find method").asRuntimeException());
            }
        } catch (MalformedURLException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription(e.getMessage()).asRuntimeException());
        } catch (ClassNotFoundException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not find class").asRuntimeException());
        } catch (NoSuchMethodException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not find method").asRuntimeException());
        } catch (InstantiationException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not create instance").asRuntimeException());
        } catch (IllegalAccessException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not access to the class").asRuntimeException());
        } catch (InvocationTargetException e) {
            responseObserver.onError(Status.UNKNOWN.augmentDescription("Could not invoke method").asRuntimeException());
        }
    }
}
