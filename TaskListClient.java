import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class TaskListClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            TaskListInterface taskList = (TaskListInterface) registry.lookup("TaskListService");

            taskList.addTask("Task 1");
            taskList.addTask("Task 2");

            List<String> allTasks = taskList.getAllTasks();
            System.out.println("All tasks: " + allTasks);

            taskList.removeTask(0);
            allTasks = taskList.getAllTasks();
            System.out.println("All tasks after removal: " + allTasks);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
