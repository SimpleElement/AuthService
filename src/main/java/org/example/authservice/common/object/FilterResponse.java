package org.example.authservice.common.object;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilterResponse {

    private String timestamp;

    private String path;

    private String method;

    private Integer status;

    private String error;

    @Override
    public String toString() {
        return "{\n" +
                "\ttimestamp: \"" + timestamp + "\",\n" +
                "\tpath: \"" + path + "\",\n" +
                "\tmethod: \"" + method + "\",\n" +
                "\tstatus: " + status + ",\n" +
                "\terror: \"" + error + "\",\n" +
                '}';
    }
}
