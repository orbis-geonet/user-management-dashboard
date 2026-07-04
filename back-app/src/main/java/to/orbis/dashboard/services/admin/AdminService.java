package to.orbis.dashboard.services.admin;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import to.orbis.dashboard.exceptions.ExportException;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.models.dto.DeleteDto;
import to.orbis.dashboard.models.dto.FileType;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.tasks.utils.RangeUtils;
import to.orbis.dashboard.utils.PageUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public abstract class AdminService<L, T> {
    private final ReportService reportService;
    private final FireStorageService fireStorageService;
    private static boolean reportInProgress = false;
    private static final AggregationOptions AGGREGATION_OPTIONS = AggregationOptions
            .builder()
            .allowDiskUse(true)
            .build();
    @SneakyThrows
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public Stream<L> getEntityStreamForList(Aggregation aggregation) {
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public long getTotalCount(JSONObject filters){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public T getOne(String id){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public T update(String id, T entity){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public T create(T entity, HttpServletRequest request){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public DeleteDto delete(String id){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public String handleImportCsvLine(List<String> headers, List<String> line, String fileName, HttpServletRequest request){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public String getExportScvHeaders(){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public Stream<String> getExportJsonLineStream(PageRequest page, String entityId){
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public Set<String> importJson(InputStream inputStream, String fileName, HttpServletRequest request) {
        throw new ExecutionControl.NotImplementedException("Method doesn't implement");
    }

    @SneakyThrows
    public List<Criteria> getCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();
        if (filters.has("id")){
            criteriaList.add(Criteria.where("id").is(new ObjectId(filters.getString("id"))));
        }
        if (filters.has("name")){
            criteriaList.add(Criteria.where("name").regex(filters.getString("name")));
        }
        if (filters.has("fullShareLink")){
            criteriaList.add(Criteria.where("fullShareLink").regex(filters.getString("fullShareLink")));
        }
        if (filters.has("uploadFileName")){
            criteriaList.add(
                    Criteria.where("uploadFileName")
                            .regex(filters.getString("uploadFileName"))
            );
        }
        var additionCriteria = getAdditionCriteria(filters);
        if (!additionCriteria.isEmpty()) {
            criteriaList.addAll(additionCriteria);
        }
        return criteriaList;
    }

    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        return new ArrayList<>();
    }

    public List<AggregationOperation> getAdditionAggregation(JSONObject filters) {
        return new ArrayList<>();
    }

    public List<L> getAll(String entityName, String sort, String range, String filter, HttpServletResponse response) {
        var filters = new JSONObject(filter);

        var offsetLimit = RangeUtils.getRange(range);
        var size = offsetLimit.get(1) - offsetLimit.get(0) + 1;
        var page = PageUtil.createPageRequest(sort, size, offsetLimit);

        var criteriaList = getCriteria(filters);

        List<AggregationOperation> aggregationList = new ArrayList<>(
                PageUtil.getSortAggregation(sort)
        );

        List<AggregationOperation> additionAggregation = getAdditionAggregation(filters);

        if (!additionAggregation.isEmpty()) {
            aggregationList.addAll(additionAggregation);
        }

        if (!criteriaList.isEmpty()) {
            criteriaList.forEach(it -> aggregationList.add(Aggregation.match(it)));
        }

        Integer count = getCount(aggregationList);
        RangeUtils.addContentRange(response, entityName, count, offsetLimit);

        aggregationList.add(Aggregation.skip((long) page.getPageNumber() * page.getPageSize()));
        aggregationList.add(Aggregation.limit(page.getPageSize()));
        return getEntityStreamForList(Aggregation.newAggregation(aggregationList).withOptions(AGGREGATION_OPTIONS))
                .collect(Collectors.toList());
    }

    private Integer getCount(List<AggregationOperation> aggregation) {
        var countAggregation = new ArrayList<>(aggregation);
        countAggregation.add(Aggregation.count().as("count"));
        var result = getTotalCount(Aggregation.newAggregation(countAggregation).withOptions(AGGREGATION_OPTIONS))
                .getMappedResults();
        if (result.isEmpty()) {
            return 0;
        } else {
            return result
                    .get(0)
                    .getCount();
        }
    }

    @Async
    public void exportCsv(String fileName, FileType fileType, int from, int till, String entityId) {
        if (reportInProgress) {
            throw new ExportException("Wait until other report is created. And try again");
        }

        int size = till;
//        int size = till - from + 1;
//        if (size > 1001) {
//            throw new ExportException("You cannot export more then 1000 items.");
//        }

        reportInProgress = true;

        String name = String.format(
                "%s_%s_from_%s_size_%s.%s",
                fileName, Instant.now().toEpochMilli(), from, size, fileType.name().toLowerCase(Locale.ROOT)
        );
        ObjectId id = reportService.createNew(name);
        try {
            PageRequest page = PageRequest.of((from/size), size, Sort.by("timestamp").descending());
            var list = getExportLineStream(fileType, page, entityId);

            var writer = fireStorageService.createWriteChanel(name, "report");

            var report = new StringBuilder();
            if (fileType.equals(FileType.CSV)) {
                report.append(getExportScvHeaders());
            } else if (fileType.equals(FileType.JSON)) {
                report.append("[");
            }

            String delimiter = "";
            if (fileType.equals(FileType.JSON)) {
                delimiter = ",";
            }

            String data = list.collect(Collectors.joining(delimiter));
            report.append(data);
            if (fileType.equals(FileType.JSON)) {
                report.append("]");
            }

            writer.write(ByteBuffer.wrap(report.toString().getBytes(StandardCharsets.UTF_8)));
            writer.close();

            reportService.markAsReady(id);
        } catch (Exception e) {
            e.printStackTrace();
            reportService.markAsError(id, e.getMessage());
        } finally {
            reportInProgress = false;
        }
    }

    private Stream<String> getExportLineStream(FileType fileType, PageRequest page, String entityId) {
        return switch (fileType) {
            case CSV -> getExportCsvLineStream(page, entityId);
            case JSON -> getExportJsonLineStream(page, entityId);
        };
    }

    @Transactional
    public Set<String> importFile(MultipartFile file, FileType fileType, HttpServletRequest request, String requiredFileName) {
        String fileName = file.getOriginalFilename();
        if (file.getOriginalFilename() == null) {
            throw new ImportException("File is empty");
        } else if (fileName != null && !fileName.contains(requiredFileName)) {
            throw new ImportException("File name is wrong. It has to start with " + fileName);
        } else if (fileName != null) {
            try {
                InputStream fileInputStream = file.getInputStream();
                return importFile(fileName, fileInputStream, fileType, request);
            } catch (IOException e) {
                log.error("importCsv: {}", e.getMessage());
                throw new ImportException(e.getMessage());
            }
        } else {
            throw new ImportException("File name is wrong.");
        }
    }

    @Transactional
    public Set<String> importFile(
            String fileName,
            InputStream fileInputStream,
            FileType fileType,
            HttpServletRequest request
    ) {
        switch (fileType) {
            case CSV:
                if (fileName.endsWith(FileType.CSV.name().toLowerCase())) {
                    return importCsv(fileInputStream, fileName, request);
                } else {
                    throw new ImportException("Wrong file type. Should be CSV");
                }
            case JSON:
                if (fileName.endsWith(FileType.JSON.name().toLowerCase())) {
                    return importJson(fileInputStream, fileName, request);
                } else {
                    throw new ImportException("Wrong file type. Should be JSON");
                }
            default:
                throw new ImportException("Unexpected file type: " + fileType);
            }

    }

    private Set<String> importCsv(
            InputStream inputStream, String fileName, HttpServletRequest request
    ) {
        Set<String> resultSet = new HashSet<>();
        var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] lineInArray = csvReader.readNext();

            List<String> headers = Arrays.stream(lineInArray)
                    .map(lineInner -> lineInner.replace("\"", "").trim())
                    .collect(Collectors.toList());

            int lineNumber = 0;
            while ((lineInArray = csvReader.readNext()) != null) {
                lineNumber++;
                List<String> line = Arrays.stream(lineInArray)
                        .map(lineInner -> lineInner.replace("\"", "").trim())
                        .collect(Collectors.toList());
                String entityId = handleImportCsvLine(headers, line, fileName, request);
                resultSet.add(entityId);
                log.info("{}: line: {} was finished", fileName, lineNumber);
            }

            return resultSet;
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
