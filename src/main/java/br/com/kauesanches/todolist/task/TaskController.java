package br.com.kauesanches.todolist.task;

import br.com.kauesanches.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private ITaskRepository repository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel model, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        model.setId((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(model.getStartAt()) || currentDate.isAfter(model.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("As datas de inicio e termino devem ser maior que a data atual");
        }
        if (model.getStartAt().isAfter(model.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de inicio deve ser menor que a data determino");
        }
        var task = this.repository.save(model);
        return ResponseEntity.status(200).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        var tasks = this.repository.findByIdUser((UUID) idUser);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        var task = repository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tarefa não enocontrada");
        }
        var idUser = request.getAttribute("idUser");

        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario não tem permissão para alterar essa tarefa!");
        }

        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = repository.save(task);
        return ResponseEntity.ok().body(repository.save(taskUpdated));
    }
}
