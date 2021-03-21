package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.model.dto.input.StaffInsertDTO;
import com.phamthehuy.doan.model.dto.input.StaffUpdateDTO;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;
import com.phamthehuy.doan.model.entity.Staff;
import com.phamthehuy.doan.service.StaffService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    private StaffRepository staffRepository;

    final
    private PasswordEncoder passwordEncoder;

    public StaffServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<StaffOutputDTO> listStaff(Integer page, Integer limit) {

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<Staff> staffList;
        if (page != null && limit != null) {
            Page<Staff> pages = staffRepository.findByDeletedFalse(PageRequest.of(page, limit));
            staffList=pages.toList();
        }
        else staffList = staffRepository.findByDeletedFalse();
        List<StaffOutputDTO> staffOutputDTOList = new ArrayList<>();
        for (Staff staff : staffList) {
            staffOutputDTOList.add(modelMapper.map(staff, StaffOutputDTO.class));
        }
        return staffOutputDTOList;
    }

    @Override
    public ResponseEntity<?> insertStaff(StaffInsertDTO staffInsertDTO) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff staff = modelMapper.map(staffInsertDTO, Staff.class);
            staff.setDob(sdf.parse(staffInsertDTO.getBirthday()));
            staff.setPass(passwordEncoder.encode(staffInsertDTO.getPass()));
            Staff newStaff = staffRepository.save(staff);
            return ResponseEntity.ok(modelMapper.map(newStaff, StaffOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Insert failed");
        }
    }

    @Override
    public ResponseEntity<?> updateStaff(StaffUpdateDTO staffUpdateDTO) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff staff = modelMapper.map(staffUpdateDTO, Staff.class);
            Staff oldStaff = staffRepository.findByStaffIdAndDeletedFalse(staffUpdateDTO.getStaffId());
            staff.setPass(oldStaff.getPass());
            staff.setDob(sdf.parse(staffUpdateDTO.getBirthday()));
            Staff newStaff = staffRepository.save(staff);
            return ResponseEntity.ok(modelMapper.map(newStaff, StaffOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Update failed");
        }
    }

    @Override
    public ResponseEntity<String> blockStaff(Integer id) {
        Staff staff = staffRepository.findByStaffIdAndDeletedFalse(id);
        if (staff == null) return ResponseEntity.badRequest().body("StaffId: " + id + " is not found");
        else {
            staff.setDeleted(true);
            staffRepository.save(staff);
            return ResponseEntity.ok("Block staff id: " + id + " successfully");
        }
    }

    @Override
    public ResponseEntity<String> activeStaff(Integer id) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (!optionalStaff.isPresent()) return ResponseEntity.badRequest().body("StaffId: " + id + " is not found");
        else {
            optionalStaff.get().setDeleted(false);
            staffRepository.save(optionalStaff.get());
            return ResponseEntity.ok("Active staff id: " + id + " successfully");
        }
    }

    @Override
    public List<StaffOutputDTO> searchStaff(String search, Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<Staff> staffList;
        if (page != null && limit != null) {
            Page<Staff> pages = staffRepository.findByNameLikeOrEmailLikeOrPhoneLikeAndDeletedFalse
                    ("%" + search + "%", "%" + search + "%", "%" + search + "%",
                            PageRequest.of(page, limit));
            staffList=pages.toList();
        }
        else staffList = staffRepository.findByNameLikeOrEmailLikeOrPhoneLikeAndDeletedFalse
                ("%" + search + "%", "%" + search + "%", "%" + search + "%");
        List<StaffOutputDTO> staffOutputDTOList = new ArrayList<>();
        for (Staff staff : staffList) {
            staffOutputDTOList.add(modelMapper.map(staff, StaffOutputDTO.class));
        }
        return staffOutputDTOList;
    }

    @Override
    public ResponseEntity<?> findOneStaff(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            return ResponseEntity.ok(modelMapper.map(staffRepository.findByStaffIdAndDeletedFalse(id),
                    StaffOutputDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("StaffId: " + id + " is not found");
        }
    }
}
