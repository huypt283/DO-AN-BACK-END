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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    final
    AdvertisementRepository adverRepository;

    //tiem nek
    final
    StaffRepository staffRepository;

    public AdvertisementServiceImpl(AdvertisementRepository adverRepository, StaffRepository staffRepository) {
        this.adverRepository = adverRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public List<AdvertisementOutputDTO> listAdvertisement(String search,
                                                          Integer page, Integer limit) {
        List<Advertisement> advertisementList;
        if(search!=null && !search.equals("")){
            advertisementList=adverRepository.findByTitleLike("%"+search+"%");
        }else{
            advertisementList=adverRepository.findAll();
        }

        //pageable
        if(page!=null && limit!=null)
            advertisementList=pageable(advertisementList,page,limit);

        //convert to AdvertisementOutputDTO
        List<AdvertisementOutputDTO> advertisementOutputDTOS = new ArrayList<>();
        for (Advertisement advertisement : advertisementList) {
            advertisementOutputDTOS.add(new AdvertisementOutputDTO(advertisement));
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
            return ResponseEntity.ok(new Message("Insert failed"));
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
            return ResponseEntity.ok(new Message("Update failed"));
        }
    }

    @Override
    public Message deleteAdvertisement(Integer id) {
        Optional<Advertisement> optionalAdvertisement = adverRepository.findById(id);
        if (optionalAdvertisement.isPresent()){
            adverRepository.delete(optionalAdvertisement.get());
            return new Message("Delete successfully");
        }else{
            return new Message("Id: " +id+" does not exist");
        }
    }

    @Override
    public ResponseEntity<?> findOneAdvertisement(Integer id) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        Optional<Advertisement> advertisementOptional=adverRepository.findById(id);
        if(advertisementOptional.isPresent())
            return ResponseEntity.ok(
                    modelMapper.map(advertisementOptional.get(), AdvertisementOutputDTO.class));
        else return ResponseEntity.ok(new Message("Advertisement id: "+id+" not found."));
    }

    private List<Advertisement> pageable(List<Advertisement> users, Integer page, Integer limit) {
        List<Advertisement> returnList = new ArrayList<>();
        if (page * limit > users.size() - 1) return returnList;
        int endIndex = Math.min((page + 1) * limit, users.size());
        for (int i = page * limit; i < endIndex; i++) {
            returnList.add(users.get(i));
        }
        return returnList;
    }
}