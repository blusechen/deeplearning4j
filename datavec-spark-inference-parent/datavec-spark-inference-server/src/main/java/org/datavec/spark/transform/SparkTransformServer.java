package org.datavec.spark.transform;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.JsonNode;
import org.datavec.spark.transform.model.Base64NDArrayBody;
import org.datavec.spark.transform.model.BatchCSVRecord;
import org.datavec.spark.transform.model.SequenceBatchCSVRecord;
import org.datavec.spark.transform.model.SingleCSVRecord;
import org.datavec.spark.transform.service.DataVecTransformService;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import play.mvc.Http;
import play.server.Server;

import java.io.IOException;

import static play.mvc.Controller.request;

/**
 * Created by kepricon on 17. 6. 20.
 */
public abstract class SparkTransformServer implements DataVecTransformService {
    @Parameter(names = {"-j", "--jsonPath"}, arity = 1)
    protected String jsonPath = null;
    @Parameter(names = {"-dp", "--dataVecPort"}, arity = 1)
    protected int port = 9000;
    @Parameter(names = {"-dt", "--dataType"}, arity = 1)
    private TransformDataType transformDataType = null;
    protected Server server;
    protected static ObjectMapper objectMapper = new ObjectMapper();


    public abstract void runMain(String[] args) throws Exception;

    /**
     * Stop the server
     */
    public void stop() {
        if (server != null)
            server.stop();
    }

    protected boolean isSequence() {
        return request().hasHeader(SEQUENCE_OR_NOT_HEADER)
                && request().getHeader(SEQUENCE_OR_NOT_HEADER).toUpperCase()
                .equals("TRUE");
    }


    protected String getHeaderValue(String value) {
        if(request().hasHeader(value))
            return request().getHeader(value);
        return null;
    }

    protected String getJsonText() {
        JsonNode tryJson = request().body().asJson();
        if (tryJson != null)
            return tryJson.toString();
        else
            return request().body().asText();
    }

    public abstract Base64NDArrayBody transformSequenceArrayIncremental(BatchCSVRecord singleCsvRecord);

    public static <R> R getObjectFromRequest(Http.Request request, Class<R> klass) throws IOException {
        String fromRPC = request.getHeader("fromRPC");
        JsonNode jsonNode2 = request().body().asJson();

        R retObject = null;

        if (fromRPC != null && fromRPC.equals("true")) {
            String json = objectMapper.readValue(jsonNode2.toString(), String.class);
            retObject = objectMapper.readValue(json.toString(), klass);
        } else {
            retObject = objectMapper.readValue(jsonNode2.toString(), klass);
        }

        return retObject;
    }
}
