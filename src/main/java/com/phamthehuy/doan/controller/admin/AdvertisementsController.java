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

    //    list quảng cáo	Get/admin/advertisements
    @GetMapping("/advertisements")
    public ResponseEntity<List<Advertisement>> listAdvertisements() {
        return ResponseEntity.ok(advertisementRepository.findAll());
    }

    //    tìm theo title	Get/admin/advertisements?title={title}
    @GetMapping(value = "/advertisements", params = "title")
    public ResponseEntity<List<Advertisement>>
    findAdvertisementsByTitle(@RequestParam("title") String title) {
        return null;
    }

    //    đăng quản cáo	Post/admin/advertisements
    @PostMapping("/advertisements")
    public ResponseEntity<?> postAdvertisement(@RequestBody Advertisement advertisement) {
        Advertisement newAdvertisement = advertisementRepository.save(advertisement);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newAdvertisement.getAdvertisementId()).toUri();

        return ResponseEntity.created(location).build();
    }

    //    sửa quảng cáo	Put/admin/advertisements
    @PutMapping("/advertisements")
    public ResponseEntity<?> updateAdvertisement(@RequestBody Advertisement advertisement) {
        Advertisement newAdvertisement = advertisementRepository.save(advertisement);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newAdvertisement.getAdvertisementId()).toUri();

        return ResponseEntity.created(location).build();
    }

//    xóa quảng cáo	Delete/admin/advertisements/id
    @DeleteMapping("/advertisements/{id}")
    public void deleteAdvertisement(@PathVariable int id){
        advertisementRepository.deleteById(id);
    }

}
