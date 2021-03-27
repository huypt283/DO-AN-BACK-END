package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.dto.input.AdvertisementInsertDTO;
import com.phamthehuy.doan.model.dto.input.AdvertisementUpdateDTO;
import com.phamthehuy.doan.model.dto.output.AdvertisementOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.service.AdvertisementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/")
public class AdvertisementController {
    final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping("advertisement")
    public List<AdvertisementOutputDTO> listAdvertisement(@RequestParam(required = false) String title,
                                                          @RequestParam(required = false) Integer page,
                                                          @RequestParam(required = false) Integer limit) {
        return advertisementService.listAdvertisement(title, page, limit);
    }
    @PostMapping("advertisement")
    public ResponseEntity<?>insertAdvertisement (@RequestBody AdvertisementInsertDTO advertisementInsertDTO) {
        return advertisementService.insertAdvertisement(advertisementInsertDTO);
    }

    @PutMapping("advertisement")
    public ResponseEntity<?> updateAdvertisement(@RequestBody AdvertisementUpdateDTO advertisementUpdateDTO) {
        return advertisementService.updateAdvertisement(advertisementUpdateDTO);
    }
    @DeleteMapping("advertisement/{id}")
    public Message deleteAdvertisement(@PathVariable Integer id) {
        return advertisementService.deleteAdvertisement(id);
    }
    @GetMapping("advertisement/{id}")
    public ResponseEntity<?> findOneAdvertisement(@PathVariable Integer id) {
        return advertisementService.findOneAdvertisement(id);
    }
}
