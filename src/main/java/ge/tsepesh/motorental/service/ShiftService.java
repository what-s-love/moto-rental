//package ge.tsepesh.motorental.service;
//
//import ge.tsepesh.motorental.dto.ShiftDto;
//import ge.tsepesh.motorental.dto.ShiftUpdateDto;
//import ge.tsepesh.motorental.model.Shift;
//import ge.tsepesh.motorental.repository.ShiftRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//@Slf4j
//public class ShiftService {
//    private final ShiftRepository shiftRepository;
//
//    public List<ShiftDto> getAllShifts();
//    public List<ShiftDto> getEnabledShifts();
//    public Shift getShiftById(Integer id);
//    public Shift updateShift(Integer id, ShiftUpdateDto dto);
//    public void toggleShiftEnabled(Integer id);
//}
//ToDo Доделать сервис, уйти с прямого использования репозитория