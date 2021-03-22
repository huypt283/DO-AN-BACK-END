package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.dao.AdvertisementRepository;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.model.dto.input.AdvertisementInsertDTO;
import com.phamthehuy.doan.model.dto.input.AdvertisementUpdateDTO;
import com.phamthehuy.doan.model.dto.output.AdvertisementOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.entity.Advertisement;
import com.phamthehuy.doan.model.entity.Staff;
import com.phamthehuy.doan.service.AdvertisementService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AdvertisementServiceImpl implements AdvertisementService {
    final
    AdvertisementRepository adverRepository;

    final
    StaffRepository staffRepository;

    public AdvertisementServiceImpl(AdvertisementRepository adverRepository, StaffRepository staffRepository) {
        this.adverRepository = adverRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public List<AdvertisementOutputDTO> listAdvertisement(Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<Advertisement> advertisementList;
        if (page != null && limit != null) {
            Page<Advertisement> pages = adverRepository.findByDeletedFalse(PageRequest.of(page, limit));
            advertisementList = pages.toList();
        } else
            advertisementList = adverRepository.findByDeletedFalse();
        List<AdvertisementOutputDTO> advertisementOutputDTOS = new ArrayList<>();
        for (Advertisement advertisement : advertisementList) {
            advertisementOutputDTOS.add(modelMapper.map(advertisement, AdvertisementOutputDTO.class));
        }

        return advertisementOutputDTOS;
    }

    @Override
    public ResponseEntity<?> insertAdvertisement(AdvertisementInsertDTO advertisementInsertDTO) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            Advertisement advertisement = modelMapper.map(advertisementInsertDTO, Advertisement.class);

            //truy van ve staff theo staff id
            Optional<Staff> staff=staffRepository.findById(advertisementInsertDTO.getStaffId());
            //gan staff cho advertisement
            if(staff.isPresent()){
                advertisement.setStaff(staff.get());
            }
            Advertisement newAdvertisement = adverRepository.save(advertisement);
            return ResponseEntity.ok(new AdvertisementOutputDTO(newAdvertisement));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Message("Insert failed"));
        }

    }

    @Override
    public ResponseEntity<?> updateAdvertisement(AdvertisementUpdateDTO advertisementUpdateDTO) {
        try{
            ModelMapper modelMapper = new ModelMapper();
            Advertisement advertisement = modelMapper.map(advertisementUpdateDTO, Advertisement.class);

            //truy van old advertisement
            Optional<Advertisement> optionalAdvertisement=adverRepository.findById(advertisementUpdateDTO.getAdvertisementId());
            Advertisement oldAdvertisement=optionalAdvertisement.get();

            //gan staff id cu
            advertisement.setStaff(oldAdvertisement.getStaff());

            Advertisement newAdvertisement = adverRepository.save(advertisement);
            return ResponseEntity.ok(new AdvertisementOutputDTO(newAdvertisement));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new Message("Update failed"));
        }
    }

    @Override
    public ResponseEntity<String> deleteAdvertisement(Integer id) {
        Advertisement advertisement = adverRepository.findByAdvertisementIdAndDeletedFalse(id);
        if (advertisement == null){
            return ResponseEntity.badRequest().body("Id: " +id+" does not exist");
        }else{
            advertisement.setDeleted(true);
            adverRepository.save(advertisement);
        }
        return ResponseEntity.ok("Success full");
    }

}
