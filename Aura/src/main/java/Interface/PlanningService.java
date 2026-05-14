package Interface;

import Model.Task;
import java.util.List;

public interface PlanningService {
    List<Task> generatePlanning(int userId, String mood);
}