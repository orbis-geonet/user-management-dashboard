package to.orbis.dashboard.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PageUtil {
    public static PageRequest createPageRequest(String sort, int size, List<Integer> offsetLimit) {
        if (!sort.equals("[]")) {
            var sortParsed = PageUtil.getValuesFromInputString(sort);
            Sort sortPage;
            if (sortParsed.get(1).equals("DESC")) {
                sortPage = Sort.by(sortParsed.get(0)).descending();
            } else {
                sortPage = Sort.by(sortParsed.get(0));
            }
            return PageRequest.of((offsetLimit.get(0)/size), size, sortPage);
        } else {
            return PageRequest.of((offsetLimit.get(0)/size), size);
        }
    }

    public static List<String>  getValuesFromInputString(String value) {
        return Arrays.stream(value
                        .replace("\"", "")
                        .replace("\"", "")
                        .replace("[", "")
                        .replace("]", "").split(","))
                .collect(Collectors.toList());
    }

    public static List<AggregationOperation> getSortAggregation(String sort) {
        if (!sort.equals("[]")) {
            var sortParsed = PageUtil.getValuesFromInputString(sort);

            List<AggregationOperation> aggregationOperations = new ArrayList<>();

            var fieldName = sortParsed.get(0);
            if (fieldName.toLowerCase().contains("name")) {
                fieldName = sortParsed.get(0) + "ToLower";
                aggregationOperations.add(
                        new FreeFormOperation("$addFields", String.format("{\"%s\": {\"$toLower\": \"$%s\"}}", fieldName, sortParsed.get(0)))
                );
            }

            if (sortParsed.get(1).equals("DESC")) {
                aggregationOperations.add(
                        Aggregation.sort(Sort.by(fieldName).descending())
                );
            } else {
                aggregationOperations.add(
                        Aggregation.sort(Sort.by(fieldName))
                );
            }

            return aggregationOperations;
        } else {
            return Collections.emptyList();
        }
    }
}
