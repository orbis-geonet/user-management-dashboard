package to.orbis.dashboard.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class GetEmailsTest {

    @SneakyThrows
    public void transferEmailsFromJsonToCsv() {
        List<TestUser> userList = new ArrayList<>();
        var fileName = "orbis-7b9a7-524b2-export";
        var filePath = Path.of("./" + fileName + ".json").toFile();
        try(JsonReader jsonReader = new JsonReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))
        ) {
            Gson gson = new GsonBuilder().create();
            jsonReader.beginObject();
            int numberOfRecords = 0;
            while (jsonReader.hasNext()){
                String name = jsonReader.nextName();

                System.out.println(name);
                if (name.equals("users")) {
                    jsonReader.beginObject();
                    name = jsonReader.nextName();
                    while (!name.equals("hereusersByGroup")) {
                        TestUser user = gson.fromJson(jsonReader, TestUser.class);
                        userList.add(user);
                        name = jsonReader.nextName();
                    }
                }

                gson.fromJson(jsonReader, Way.class);
                numberOfRecords++;
            }
            jsonReader.endObject();
            System.out.println("Total Records Found : "+numberOfRecords);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var resultList = userList.
                stream().filter(it -> !Strings.isBlank(it.email))
                .collect(Collectors.toList());


        FileWriter writer = new FileWriter("./" + fileName + ".csv");
        writer.append("email;username;\n");

        resultList
                .stream()
                .map(it -> it.getEmail() + ";" + it.username + ";\n")
                .forEach(it -> {
                    try {
                        writer.append(it);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        writer.close();
        System.out.printf("");
    }

    private static class Way {
        private long id;
        private String type;
        List<Long> nodes;
        public long GetId() {
            return id;
        }
    }

    @Builder
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TestUser {
        String email;
        String username;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestUser testUser = (TestUser) o;
            return Objects.equals(email, testUser.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email);
        }
    }
}
