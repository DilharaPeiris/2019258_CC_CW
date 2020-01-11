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
                switch (request.getQueryStringParameters().get("action")) {
                    case "car":
                        System.out.println("Switch execute case car");
                        System.out.println(getCarById(Integer.parseInt(request.getQueryStringParameters().get("carId"))));
                        String stringCar = objectMapper.writeValueAsString(getCarById(Integer.parseInt(request.getQueryStringParameters().get("carId"))));
                        responseEvent.setBody(stringCar);
                        break;
                    case "cars":
                        String stringCarList = objectMapper.writeValueAsString(getAllCarCars());
                        responseEvent.setBody(stringCarList);
                        break;
                    case "options":
                        car =getCarById(Integer.parseInt(request.getQueryStringParameters().get("carId")));
                        String stringOptionList=String.join(",", car.getOptions());
                        responseEvent.setBody("Model "+car.getModel()+" car options : " + stringOptionList);
                        break;
                    case "price":
                         car =getCarById(Integer.parseInt(request.getQueryStringParameters().get("carId")));
                        Double price=getCarById(Integer.parseInt(request.getQueryStringParameters().get("carId"))).getPrice();
                        responseEvent.setBody("Model "+car.getModel()+" car price : " + price);
                        break;
                }
            }
            responseEvent.setStatusCode(201);

        } catch (Exception exp) {
            System.out.println("Exception thrown");
            System.out.println(exp.getCause());
            System.out.println(exp.getMessage());
            System.out.println(exp.getStackTrace());
            responseEvent.setStatusCode(500);
            responseEvent.setBody("Exception thrown with the Spring Cloud Function with message: " + "|" + exp.getMessage());
        }

        return responseEvent;
    }

    private CarEntity getCarById(int carId) {
        return mapper.load(CarEntity.class, carId);
    }

    private List<CarEntity> getAllCarCars() {
        PaginatedScanList<CarEntity> carScanList = mapper.scan(CarEntity.class, new DynamoDBScanExpression());
        return carScanList.stream().collect(Collectors.toList());
    }

}
