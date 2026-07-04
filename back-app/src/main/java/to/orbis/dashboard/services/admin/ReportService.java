package to.orbis.dashboard.services.admin;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.models.dto.list.ReportListDto;
import to.orbis.dashboard.models.entity.Report;
import to.orbis.dashboard.models.entity.types.ReportStatus;
import to.orbis.dashboard.repositories.ReportRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.utils.PageUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final FireStorageService fireStorageService;

    public List<ReportListDto> getAll(String sort, String range, HttpServletResponse response) {

        var offsetLimit = PageUtil.getValuesFromInputString(range)
                .stream().map(Integer::valueOf)
                .collect(Collectors.toList());
        var count = reportRepository.count();
        var size = offsetLimit.get(1) - offsetLimit.get(0) + 1;
        var r = offsetLimit.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));
        response.setHeader("Content-Range", "reports" + " " + r + "/" + count);
        response.setHeader("Access-Control-Expose-Headers", "Content-Range");

        PageRequest page = PageUtil.createPageRequest(sort, size, offsetLimit);

        return reportRepository.findAll(page)
                .stream()
                .map(ReportListDto::new)
                .collect(Collectors.toList());
    }

    public ObjectId createNew(String name) {
        var report = new Report();
        report.setName(name);
        report.setTimestamp(Instant.now());
        report.setStatus(ReportStatus.CREATING);
        report.setId(new ObjectId());
        reportRepository.save(report);
        return report.getId();
    }

    public void markAsReady(ObjectId id) {
        reportRepository.findById(id)
                .ifPresent(it -> {
                    it.setStatus(ReportStatus.READY);
                    it.setTimestamp(Instant.now());
                    reportRepository.save(it);
                });
    }

    public void markAsError(ObjectId id, String error) {
        reportRepository.findById(id)
                .ifPresent(it -> {
                    it.setStatus(ReportStatus.ERROR);
                    it.setErrorMessage(error);
                    it.setTimestamp(Instant.now());
                    reportRepository.save(it);
                });
    }

    @SneakyThrows
    public void download(String id, HttpServletResponse response) {
        var reportOptional = reportRepository.findById(new ObjectId(id));
        if (reportOptional.isPresent()) {
            var report = reportOptional.get();
            String fileName =
                    report.getName().contains("/") ?
                            report.getName().split("/")[1] :
                            report.getName();

            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            var file = fireStorageService.getFile(fileName,"report");

            try (var bufferReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file)))) {
                var line = "";
                while ((line = bufferReader.readLine()) != null) {
                    response.getWriter().write(line + "\n");
                }

                reportRepository.delete(report);
            }
        }
    }

    public void deleteReportsWithError() {
        reportRepository.deleteAllByStatusIn(List.of(ReportStatus.ERROR, ReportStatus.CREATING));
    }
}
