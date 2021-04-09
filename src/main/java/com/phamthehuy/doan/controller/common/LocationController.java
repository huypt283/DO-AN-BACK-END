package com.phamthehuy.doan.controller.common;

import com.phamthehuy.doan.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private LocationService locationService;

    @GetMapping("/cities")
    public ResponseEntity<?> getCities() throws Exception {
        return new ResponseEntity<>(locationService.getCities(), HttpStatus.OK);
    }

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts(@RequestParam(name = "city", required = false) Integer city) throws Exception {
        return new ResponseEntity<>(locationService.getDistricts(city), HttpStatus.OK);
    }

    @GetMapping("/wards")
    public ResponseEntity<?> getWards(@RequestParam(name = "district", required = false) Integer district) throws Exception {
        return new ResponseEntity<>(locationService.getWards(district), HttpStatus.OK);
    }
}
