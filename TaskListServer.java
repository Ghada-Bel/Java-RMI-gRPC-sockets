import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class TaskListServer implements TaskListInterface {
    private List<String> tasks;

    public TaskListServer() {
        tasks = new ArrayList<>();
    }

    @Override
    public synchronized void addTask(String task) throws RemoteException {
        tasks.add(task);
        System.out.println("Added task: " + task);
    }

    @Override
    public synchronized void removeTask(int taskId) throws RemoteException {
        if (taskId >= 0 && taskId < tasks.size()) {
            String removedTask = tasks.remove(taskId);
            System.out.println("Removed task: " + removedTask);
        } else {
            throw new RemoteException("Invalid task ID");
        }
    }

    @Override
    public synchronized List<String> getAllTasks() throws RemoteException {
        return new ArrayList<>(tasks);
    }

    public static void main(String[] args) {
        try {
            TaskListServer server = new TaskListServer();
            TaskListInterface stub = (TaskListInterface) UnicastRemoteObject.exportObject(server, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("TaskListService", stub);

            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
