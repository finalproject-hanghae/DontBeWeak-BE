package com.finalproject.dontbeweak.service;

import com.finalproject.dontbeweak.dto.ApiRequestDto;
import com.finalproject.dontbeweak.model.Api;
import com.finalproject.dontbeweak.repository.ApiRepository;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class ApiService {

    private final ApiRepository apiRepository;

    public List<ApiRequestDto> parsing(StringBuilder result) {
        try {
            JSONObject Object;
            //json 객체 생성
            JSONParser jsonParser = new JSONParser();
            //json 파싱 객체 생성
            JSONObject jsonObject = (JSONObject) jsonParser.parse(result.toString());

            //데이터 분해
            JSONObject parseResponse = (JSONObject) jsonObject.get("body");
            JSONArray array = (JSONArray) parseResponse.get("items");
            for (int i = 0; i < array.size(); i++) {
                Object = (JSONObject) array.get(i);

                String product = (String) Object.get("PRDUCT");
                if (product == null) {
                    product = "";
                }

                String srv_use = (String) Object.get("SRV_USE");
                if (srv_use == null) {
                    srv_use = "";
                }

                Api api = Api.builder()
                        .PRDUCT(product)
                        .SRV_USE(srv_use)
                        .build();
                apiRepository.save(api);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Api> apiList = apiRepository.findAll();
        List<ApiRequestDto> requestDtos = new ArrayList<>();
        for(Api api: apiList){
            ApiRequestDto apiRequestDto1 = new ApiRequestDto(api.getPRDUCT(), api.getSRV_USE());
            requestDtos.add(apiRequestDto1);
        }
        return requestDtos;
    }

}
