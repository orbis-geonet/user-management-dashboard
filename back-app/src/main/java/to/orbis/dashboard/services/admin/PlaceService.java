package to.orbis.dashboard.services.admin;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import to.orbis.dashboard.config.security.JwtService;
import to.orbis.dashboard.exceptions.ImportException;
import to.orbis.dashboard.exceptions.NoDataException;
import to.orbis.dashboard.models.dto.*;
import to.orbis.dashboard.models.dto.list.PlaceListDto;
import to.orbis.dashboard.models.entity.Count;
import to.orbis.dashboard.models.entity.Place;
import to.orbis.dashboard.models.entity.types.MediaType;
import to.orbis.dashboard.models.entity.types.PlaceType;
import to.orbis.dashboard.repositories.GroupRepository;
import to.orbis.dashboard.repositories.PlaceRepository;
import to.orbis.dashboard.repositories.UserRepository;
import to.orbis.dashboard.services.FireStorageService;
import to.orbis.dashboard.services.GooglePlaceService;
import to.orbis.dashboard.services.MediaUploaderService;
import to.orbis.dashboard.utils.CsvUtil;
import to.orbis.dashboard.utils.ShareLinkUtil;
import to.orbis.dashboard.utils.SlugUtils;
import to.orbis.dashboard.utils.mappers.PointMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class PlaceService extends AdminService<PlaceListDto, PlaceDto>{
    private final PlaceRepository placeRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    private final GooglePlaceService googlePlaceService;

    private final JwtService jwtService;
    private final CloseableHttpClient httpClient;
    private final MediaUploaderService mediaUploaderService;

    @Value("${app.link.place}")
    private String shareLink;

    @Value("${app.posting.main-app-link}")
    private String mainAppLink;

    public PlaceService(
            ReportService reportService, FireStorageService fireStorageService, PlaceRepository placeRepository, GroupRepository groupRepository, UserRepository userRepository, UserService userService, MongoTemplate mongoTemplate, GooglePlaceService googlePlaceService, JwtService jwtService, CloseableHttpClient httpClient, MediaUploaderService mediaUploaderService) {
        super(reportService, fireStorageService);
        this.placeRepository = placeRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.mongoTemplate = mongoTemplate;
        this.googlePlaceService = googlePlaceService;
        this.jwtService = jwtService;
        this.httpClient = httpClient;
        this.mediaUploaderService = mediaUploaderService;
    }

    @Override
    public Stream<PlaceListDto> getEntityStreamForList(Aggregation aggregation) {
        return mongoTemplate
                .aggregate(aggregation, Place.class, Place.class)
                .getMappedResults()
                .stream()
                .map(PlaceListDto::new);
    }

    @Override
    public List<Criteria> getAdditionCriteria(JSONObject filters) {
        var criteriaList = new ArrayList<Criteria>();

        if (filters.has("imageUploadStatus")) {
            MediaUploadStatus imageUploadStatus = MediaUploadStatus.valueOf(filters.getString("imageUploadStatus"));
            criteriaList.add(
                    Criteria.where(Place.Fields.imageUploadStatus.name()).is(imageUploadStatus)
            );
        }

        return criteriaList;
    }

    @Override
    public AggregationResults<Count> getTotalCount(Aggregation aggregation) {
        return mongoTemplate.aggregate(aggregation, Place.class, Count.class);
    }

    @Override
    public long getTotalCount(JSONObject filters) {
        if (filters.has("id")){
            return placeRepository.countById(new ObjectId(filters.getString("id")));
        }
        if (filters.has("name")){
            return placeRepository.countByNameIsLike(filters.getString("name"));
        }
        if (filters.has("fullShareLink")){
            return placeRepository.countByFullShareLink(filters.getString("fullShareLink"));
        }
        return placeRepository.count();
    }

    @Override
    public PlaceDto getOne(String id) {
        return placeRepository.findById(new ObjectId(id))
                .map(it -> {
                    var place = new PlaceDto(it);
                    groupRepository.findOneByGroupKey(it.getDominantGroupKey())
                            .ifPresent(p -> place.setDominantGroup(
                                    new PlaceShortDto(p.getId().toHexString(), p.getName(), p.getDescription(), p.getGroupKey(), p.isDeleted())
                            ));
                    groupRepository.findOneByGroupKey(it.getGroupCreatedKey())
                            .ifPresent(p -> place.setGroupCreated(
                                    new PlaceShortDto(p.getId().toHexString(), p.getName(), p.getDescription(), p.getGroupKey(), p.isDeleted())
                            ));
                    userRepository.findOneByUserKey(it.getUserCreatedKey())
                            .ifPresent(u -> place.setUserCreated(
                                    new UserShortDto(u.getId().toHexString(), u.getEmail(), u.getDisplayName(), u.getUserKey(), u.isDeleted())
                            ));
                    return place;
                })
                .orElseThrow(() -> new NoDataException("There is no place with id=" + id));
    }

    @Override
    public PlaceDto update(String id, PlaceDto entity) {
        var place = entity.toPlace();
        log.debug("update: place={}", place.toString());

        place.setGoogleAddress(googlePlaceService.getAddress(place.getCoordinates()));
        place.setSlug(generateSlug(place));

        placeRepository.save(place);
        return new PlaceDto(place);
    }

    @Override
    public PlaceDto create(PlaceDto entity, HttpServletRequest request) {
        var place = entity.toPlace();
        log.debug("create: place={}", place.toString());
        place.setId(new ObjectId());
        if (place.getPlaceKey() == null || place.getPlaceKey().equals("")) {
            place.setPlaceKey(place.getId().toHexString());
        }

        if (place.getTimestamp() == null) {
            place.setTimestamp(Instant.now());
        }

        if (place.getFullShareLink() == null) {
            place.setFullShareLink(generateShareLink(place));
        }

        place.setGoogleAddress(googlePlaceService.getAddress(place.getCoordinates()));

        place.setDeleted(false);
        place.setUserCreatedKey(userService.getUserKeyFromRequest(request));
        place.setCreateTimestamp(Instant.now());
        place.setSlug(generateSlug(place));

        placeRepository.save(place);
        return new PlaceDto(place);
    }

    @Override
    public DeleteDto delete(String id) {
        var groupOptional = placeRepository.findById(new ObjectId(id));
        if (groupOptional.isPresent()) {
            var group = groupOptional.get();
            group.setDeleted(true);
            placeRepository.save(group);
        }
        return new DeleteDto(id);
    }

    @Override
    public String handleImportCsvLine(
            List<String> headers, List<String> line, String fileName, HttpServletRequest request) {
        var id = new ObjectId();
        var place = new Place();
        if (!line.isEmpty()) {
            if (!line.get(0).isEmpty()) {
                id = new ObjectId(line.get(0));
                var placeOptional = placeRepository.findById(id);

                if (placeOptional.isPresent()) {
                    place = placeOptional.get();
                } else {
                    place.setId(id);
                }
            } else {
                place.setId(id);
            }

            var point = new PointDto();
            for (int i = 1; i < headers.size(); i++) {
                switch (headers.get(i)) {
                    case "id": {
                        place.setId(new ObjectId(line.get(i)));
                        break;
                    }
                    case "name": {
                        place.setName(line.get(i));
                        break;
                    }
                    case "description": {
                        place.setDescription(line.get(i));
                        break;
                    }
                    case "placeKey": {
                        place.setPlaceKey(line.get(i));
                        break;
                    }
                    case "groupKey": {
                        place.setDominantGroupKey(line.get(i));
                        break;
                    }
                    case "type": {
                        try {
                            place.setType(PlaceType.valueOf(line.get(i)));
                        } catch (Exception e) {
                            place.setType(PlaceType.BUILDING);
                        }
                        break;
                    }
                    case "source": {
                        place.setSource(line.get(i));
                        break;
                    }
                    case "address": {
                        place.setAddress(line.get(i));
                        break;
                    }
                    case "phone": {
                        place.setPhone(line.get(i));
                        break;
                    }
                    case "coordinates(lng)": {
                        if (line.get(i) != null && !line.get(i).isEmpty()) {
                            point.setLng(Double.parseDouble(line.get(i)));
                        }
                        break;
                    }
                    case "coordinates(lat)": {
                        if (line.get(i) != null && !line.get(i).isEmpty()) {
                            point.setLat(Double.parseDouble(line.get(i)));
                        }
                        break;
                    }
                    case "link": {
                        String link = line.get(i);
                        if (link != null && !link.isEmpty()) {
                            place.setUploadingLink(line.get(i));
                            place.setImageUploadStatus(MediaUploadStatus.WAITING);
                        } else {
                            place.setImageUploadStatus(MediaUploadStatus.UPLOADED);
                        }
                        break;
                    }
                    case "userCreatedKey": {
                        place.setUserCreatedKey(line.get(i));
                        break;
                    }
                    default: {
                        log.error("handleImportCsvLine: Known header name={} value={}", headers.get(i), line.get(i));
                        break;
                    }
                }
            }

            if (point.getLat() != null && point.getLng() != null ) {
                place.setCoordinates(PointMapper.toGeoJsonPoint(point));
            }

            place.setShareLink(generateShareLink(place));
            place.setTimestamp(Instant.now());
            place.setPlaceKey(place.getId().toHexString());
            place.setDeleted(false);
            place.setCreateTimestamp(Instant.now());
            if (place.getUserCreatedKey() == null || place.getUserCreatedKey().isEmpty()) {
                place.setUserCreatedKey(userService.getUserKeyFromRequest(request));
            }
            place.setUploadFileName(fileName);
            place.setSlug(generateSlug(place));
            log.debug("handleImportCsvLine: place={}", place);
            placeRepository.save(place);
            return place.getPlaceKey();
        } else {
            throw new ImportException("Bad data");
        }
    }

    @Override
    public String getExportScvHeaders() {
        return "\"id\",\"name\",\"description\",\"groupKey\",\"type\",\"source\",\"address\",\"phone\",\"coordinates(lng)\",\"coordinates(lat)\",\"uploadFileName\",\"userCreatedKey\",\n";
    }

    @Override
    public Stream<String> getExportCsvLineStream(PageRequest page, String entityId) {
//        return placeRepository.findAll(page)
        return placeRepository.findAllByUploadFileNameIsLike("places_to_add_271224_")
                .stream()
                .map(this::createExportLine);
    }

    private String generateShareLink(Place place) {
        return ShareLinkUtil.generateShareLink(
                shareLink, place.getName(), placeRepository.countByName(place.getName())
        );
    }

    private String createExportLine(Place place) {
        var location = PointMapper.toPointDto(place.getCoordinates());
        return String.format(
                "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\n",
                CsvUtil.setCsvValue(place.getId().toHexString()),
                CsvUtil.setCsvValue(place.getName()),
                CsvUtil.setCsvValue(place.getDescription()),
                CsvUtil.setCsvValue(place.getDominantGroupKey()),
                CsvUtil.setCsvValue(place.getType().toString()),
                CsvUtil.setCsvValue(place.getSource()),
                CsvUtil.setCsvValue(place.getAddress()),
                CsvUtil.setCsvValue(place.getPhone()),
                location == null ? "" : CsvUtil.setCsvValue(location.getLng().toString()),
                location == null ? "" : CsvUtil.setCsvValue(location.getLat().toString()),
                CsvUtil.setCsvValue(place.getUploadFileName()),
                CsvUtil.setCsvValue(place.getUserCreatedKey())
        );
    }


    public void uploadPlaceImages() {
        List.of(0, 1, 2, 3, 4, 5)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 50);
                    uploadPlaceImages(pageRequest);
                });
    }

    public void uploadPlaceImages(PageRequest pageRequest) {
        String folder = "placePictures";

        var result = placeRepository.findAllByImageUploadStatus(MediaUploadStatus.WAITING, pageRequest)
                .stream().peek(place -> {
                    String link = place.getUploadingLink();

                    MediaUploadResponseDto responseDto = mediaUploaderService.uploadMedia(link, folder, MediaType.IMAGE);

                    if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.UPLOADED)) {
                        place.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        place.setImageName(responseDto.getFileName());
                        log.info("uploadPlaceImages: place image was uploaded. Place {}. File name: {}.", place.getPlaceKey(), responseDto.getFileName());
                    } else if (responseDto.getMediaUploadStatus().equals(MediaUploadStatus.ERROR)) {
                        place.setImageUploadStatus(responseDto.getMediaUploadStatus());
                        place.setErrorMessage(responseDto.getErrorMessage());
                        log.info("uploadPlaceImages: place image was NOT uploaded. Place {}. Error: {}.", place.getPlaceKey(), responseDto.getErrorMessage());
                    }
                })
                .toList();

        placeRepository.saveAll(result);
    }

    public void generateSlugs() {
        List.of(0, 1, 2, 3, 4, 5)
                .parallelStream()
                .forEach(it -> {
                    PageRequest pageRequest = PageRequest.of(it, 50);
                    generateSlug(pageRequest);
                });
    }

    public void generateSlug(PageRequest pageRequest) {
        var result = placeRepository.findAllBySlugIsNull(pageRequest)
                .stream()
                .peek(place -> {
                    var slug = generateSlug(place);
                    place.setSlug(slug);
                })
                .toList();

        placeRepository.saveAll(result);
    }


    private String generateSlug(Place place) {
        var emptySlug = SlugUtils.createEmptySug(place.getName());
        var count = placeRepository.countByEmptySlug(emptySlug);
        place.setEmptySlug(emptySlug);
        return SlugUtils.getSlugNames(emptySlug, place.getPlaceKey(), count);
    }

    public void polygonCalculations(HttpServletRequest request) {
        try {
            var userKey = userService.getUserKeyFromRequest(request);
            var jwtToken = jwtService.createServerToken(userKey);

            var uriBuilder = new URIBuilder(mainAppLink + "/polygon-calculations/trigger");
            var httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader("Accept", "application/json;charset=UTF-8");
            httpGet.setHeader("Content-type", "application/json;charset=UTF-8");
            httpGet.setHeader("Accept-Charset", "UTF-8, ISO-8859-1");
            httpGet.setHeader("Authorization", "Bearer " + jwtToken);

            var response = httpClient.execute(httpGet);
            if (response != null && response.getStatusLine().getStatusCode() == 200) {
                log.info("polygonCalculations: finished is ok");
            } else {
                assert response != null;
                log.error("polygonCalculations: finished is with error. code: " + response.getStatusLine().getStatusCode());
            }

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
