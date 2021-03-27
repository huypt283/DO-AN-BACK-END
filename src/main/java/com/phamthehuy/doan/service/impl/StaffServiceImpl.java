package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.dao.CustomerRepository;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.model.dto.input.StaffInsertDTO;
import com.phamthehuy.doan.model.dto.input.StaffUpdateDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;
import com.phamthehuy.doan.model.entity.Staff;
import com.phamthehuy.doan.service.StaffService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StaffServiceImpl implements StaffService {
    final
    StaffRepository staffRepository;

    final
    PasswordEncoder passwordEncoder;

    final
    CustomerRepository customerRepository;

    public StaffServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<StaffOutputDTO> listStaff(String search, String sort,
                                          Integer page, Integer limit) {
        //find all and sort
        List<Staff> staffList = new ArrayList<>();
        if(sort!=null && !sort.equals("")){
            if(sort.equalsIgnoreCase("desc")){
                staffList=staffRepository.findByDeletedFalse(Sort.by("name").descending());
            }else{
                staffList=staffRepository.findByDeletedFalse(Sort.by("name").ascending());
            }
        }else staffList=staffRepository.findByDeletedFalse();

        //search
        if(search!=null && !search.equals("")){
            Set<Staff> searchStaff=new HashSet<>();
            List<Staff> staffName=staffRepository.findByNameLikeAndDeletedFalse("%"+search+"%");
            List<Staff> staffNameFilter=filter(staffName, staffList);
            searchStaff.addAll(staffNameFilter);

            List<Staff> staffEmail=staffRepository.findByEmailLikeAndDeletedFalse("%"+search+"%");
            List<Staff> staffEmailFilter=filter(staffEmail, staffList);
            searchStaff.addAll(staffEmailFilter);

            List<Staff> staffPhone=staffRepository.findByPhoneLikeAndDeletedFalse("%"+search+"%");
            List<Staff> staffPhoneFilter=filter(staffPhone, staffList);
            searchStaff.addAll(staffPhoneFilter);

            List<Staff> searchStaffList=new ArrayList<>(searchStaff);
            staffList=filter(staffList, searchStaffList);
        }

        //pageable
        if(page!=null && limit!=null){
            staffList=pageable(staffList, page,limit);
        }

        //convert sang StaffOutputDTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<StaffOutputDTO> staffOutputDTOList = new ArrayList<>();
        for (Staff staff : staffList) {
            StaffOutputDTO staffOutputDTO=modelMapper.map(staff, StaffOutputDTO.class);
            staffOutputDTO.setDob(staff.getDob().getTime());
            staffOutputDTOList.add(staffOutputDTO);
        }
        return staffOutputDTOList;
    }

    @Override
    public ResponseEntity<?> insertStaff(StaffInsertDTO staffInsertDTO) {
        try {
            if(customerRepository.findByEmail(staffInsertDTO.getEmail())!=null)
                return ResponseEntity.ok(new Message("Email is already in use"));
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff staff = modelMapper.map(staffInsertDTO, Staff.class);
            staff.setDob(new Date((staffInsertDTO.getBirthday())));
            staff.setPass(passwordEncoder.encode(staffInsertDTO.getPass()));
            Staff newStaff = staffRepository.save(staff);
            return ResponseEntity.ok(modelMapper.map(newStaff, StaffOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new Message("Insert failed"));
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
            staff.setDob(new Date(staffUpdateDTO.getBirthday()));
            Staff newStaff = staffRepository.save(staff);
            return ResponseEntity.ok(modelMapper.map(newStaff, StaffOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new Message("Update failed"));
        }
    }

    @Override
    public Message blockStaff(Integer id) {
        Staff staff = staffRepository.findByStaffIdAndDeletedFalse(id);
        if (staff == null) return new Message("Block staff id: " + id + " failed");
        else {
            staff.setDeleted(true);
            staffRepository.save(staff);
            return new Message("Block staff id: " + id + " successfully");
        }
    }

    @Override
    public Message activeStaff(Integer id) {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (!optionalStaff.isPresent()) return new Message("StaffId: " + id + " is not found");
        else {
            optionalStaff.get().setDeleted(false);
            staffRepository.save(optionalStaff.get());
            return new Message("Active staff id: " + id + " successfully");
        }
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
            return ResponseEntity.ok(new Message("StaffId: " + id + " is not found"));
        }
    }

    @Override
    public Message deleteStaffs() {
        List<Staff> staffList=staffRepository.findByDeletedTrue();
        for(Staff staff:staffList){
            staffRepository.delete(staff);
        }
        return new Message("Deleted successfully");
    }

    public List<Staff> filter(List<Staff> minList, List<Staff> maxList) {
        List<Staff> newList = new ArrayList<>();
        for (Staff article : minList) {
            if (maxList.contains(article)) newList.add(article);
        }
        return newList;
    }

    private List<Staff> pageable(List<Staff> users, Integer page, Integer limit) {
        List<Staff> returnList = new ArrayList<>();
        if (page * limit > users.size() - 1) return returnList;
        int endIndex = Math.min((page + 1) * limit, users.size());
        for (int i = page * limit; i < endIndex; i++) {
            returnList.add(users.get(i));
        }
        return returnList;
    }
}
