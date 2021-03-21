package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.dao.AdvertisementRepository;
import com.phamthehuy.doan.model.entity.Advertisement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdvertisementsController {

    final
    AdvertisementRepository advertisementRepository;

    @Autowired
    public AdvertisementsController(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    @GetMapping("/advertisements")
    public ResponseEntity<List<Advertisement>> listAdvertisements() {
        return ResponseEntity.ok(advertisementRepository.findAll());
    }

    @GetMapping(value = "/advertisements", params = "title")
    public ResponseEntity<List<Advertisement>>
    findAdvertisementsByTitle(@RequestParam("title") String title) {
        return null;
    }

    @PostMapping("/advertisements")
    public ResponseEntity<?> postAdvertisement(@RequestBody Advertisement advertisement) {
        Advertisement newAdvertisement = advertisementRepository.save(advertisement);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newAdvertisement.getAdvertisementId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping("/advertisements")
    public ResponseEntity<?> updateAdvertisement(@RequestBody Advertisement advertisement) {
        Advertisement newAdvertisement = advertisementRepository.save(advertisement);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newAdvertisement.getAdvertisementId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/advertisements/{id}")
    public void deleteAdvertisement(@PathVariable int id){
        advertisementRepository.deleteById(id);
    }

}
