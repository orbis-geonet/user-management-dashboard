package to.orbis.dashboard.controllers;

import jdk.jshell.spi.ExecutionControl;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface AdminController<L, T> {

    List<L> getAll(String sort, String range, String filter, HttpServletResponse response);

    @SneakyThrows
    default T getOne(String id) {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default T update(String id, T entity) {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default DeleteDto delete(String id) {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default T create(T entity, HttpServletRequest request) {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default Long getCount() {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default void exportCsv(FileType fileType, int from, int till) {
        throw new ExecutionControl.NotImplementedException("exportCsv doesn't implement");
    }

    @SneakyThrows
    default void importCsv(FileType fileType, MultipartFile file, HttpServletRequest request){
        throw new ExecutionControl.NotImplementedException("importCsv doesn't implement");
    }
}
