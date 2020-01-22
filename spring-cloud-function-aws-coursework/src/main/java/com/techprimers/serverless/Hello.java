package com.techprimers.serverless;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Hello implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    @Autowired
    private AmazonDynamoDB dynamoDBClient;

    @Autowired
    private DynamoDBMapper mapper;


    private DynamoDB dynamoDb;
    private Table carTable;


    @PostConstruct
    private void init() {

        dynamoDb = new DynamoDB(dynamoDBClient);
        carTable = dynamoDb.getTable("car");

    }


    @Override
    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent request) {
        System.out.println("APIGatewayProxyResponseEvent");
        System.out.println(request.getHttpMethod());
        System.out.println(request.getPath());
        System.out.println(request.getPathParameters());
        System.out.println(request.getQueryStringParameters());
        System.out.println(request.getBody());
        System.out.println(request.getResource());

        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (request.getHttpMethod().equals("GET")) {
                System.out.println("HttpMethod Hits");
                CarEntity car;
                String method = String.valueOf(request.getQueryStringParameters().get("method"));
                if (method.equals("car")) {
                    System.out.println("Switch execute case car");
                    System.out.println(getCarModelByCarId(Integer.parseInt(request.getQueryStringParameters().get("carId"))));
                    String stringCar = objectMapper.writeValueAsString(getCarModelByCarId(Integer.parseInt(request.getQueryStringParameters().get("carId"))));
                    responseEvent.setBody(stringCar);
                }else if(method.equals("allcarModels")){
                    String stringCarList = objectMapper.writeValueAsString(getAllCarCarModels());
                    responseEvent.setBody(stringCarList);
                }else if(method.equals("caroptionslist")){
                    car =getCarModelByCarId(Integer.parseInt(request.getQueryStringParameters().get("carId")));
                    String stringOptionList=String.join(",", car.getOptions());
                    responseEvent.setBody("Vehicle  "+car.getModel()+" car options = " + stringOptionList);
                }else if(method.equals("carprice")){
                    car =getCarModelByCarId(Integer.parseInt(request.getQueryStringParameters().get("carId")));
                    Double price=getCarModelByCarId(Integer.parseInt(request.getQueryStringParameters().get("carId"))).getPrice();
                    responseEvent.setBody("Vehicle "+car.getModel()+" car price = " + price);
                }
            }
            responseEvent.setStatusCode(201);

        } catch (Exception exp) {
            responseEvent.setStatusCode(500);
            responseEvent.setBody("Exception thrown " + "|" + exp.getMessage());
        }
        return responseEvent;
    }

    private CarEntity getCarModelByCarId(int carId) {
        return mapper.load(CarEntity.class, carId);
    }

    private List<CarEntity> getAllCarCarModels() {
        PaginatedScanList<CarEntity> carScanList = mapper.scan(CarEntity.class, new DynamoDBScanExpression());
        return carScanList.stream().collect(Collectors.toList());
    }

}
