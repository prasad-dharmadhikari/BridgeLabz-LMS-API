package com.bridgelabz.lmsapi.service;

import com.bridgelabz.lmsapi.config.ApplicationConfiguration;
import com.bridgelabz.lmsapi.dto.HiredCandidateDTO;
import com.bridgelabz.lmsapi.dto.MailDTO;
import com.bridgelabz.lmsapi.response.Response;
import com.bridgelabz.lmsapi.exception.LmsApiApplicationException;
import com.bridgelabz.lmsapi.model.HiredCandidate;
import com.bridgelabz.lmsapi.repository.HiredCandidateRepository;
import com.bridgelabz.lmsapi.util.RabbitMQ;
import com.bridgelabz.lmsapi.util.Status;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Service to save data in batch, send mails, and update candidate status
 */
@Service
public class HiredCandidateService implements IHiredCandidateService {

    @Autowired
    private HiredCandidateRepository hiredCandidateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RabbitMQ rabbitMQ;

    @Autowired
    private MailDTO mailDTO;

    String personalInfo = "<html><body><p><a href='http://localhost:8080/fellowshipcandidate/jointhecandidate'><img src='cid:identifier11'></a></p></body></html>";
    String bankInfo = "<html><body><p><a href='http://localhost:8080/fellowshipcandidate/educationalinfo'><img src='cid:identifier22'></a></p></body></html>";
    String educationalInfo = "<html><body><p><a href='http://localhost:8080/fellowshipcandidate/bankinfo'><img src='cid:identifier33'></a></p></body></html>";

    /**
     * @param file
     * @return Data saved successfully
     * @throws LmsApiApplicationException
     */
    @Override
    public Response saveDataInBatchToDatabase(MultipartFile file) throws LmsApiApplicationException {
        HiredCandidateDTO hiredCandidateDTO = new HiredCandidateDTO();
        boolean flag = true;
        try (InputStream fis = file.getInputStream()) {
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
            //Iterate through each rows one by one
            Iterator rows = sheet.rowIterator();
            XSSFCell cell;
            //For each row, iterate through all the columns
            while (rows.hasNext()) {
                XSSFRow row = (XSSFRow) rows.next();
                Iterator cells = row.cellIterator();
                if (!flag) {
                    while (cells.hasNext()) {
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setId((long) cell.getNumericCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setFirstName(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setMiddleName(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setLastName(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setEmail(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setHiredCity(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setDegree(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setHiredDate(cell.getDateCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setMobileNumber((long) cell.getNumericCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setPermanentPincode((int) cell.getNumericCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setHiredLab(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setAttitude(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setCommunicationRemark(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setKnowledgeRemark(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setAggregateRemark(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setStatus(cell.getStringCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setCreatorStamp(cell.getDateCellValue());
                        cell = (XSSFCell) cells.next();
                        hiredCandidateDTO.setCreatorUser(cell.getStringCellValue());
                        save(hiredCandidateDTO);
                    }
                }
                flag = false;
            }
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
        return new Response(105, ApplicationConfiguration.getMessageAccessor().getMessage("105"));
    }

    /**
     * @return data of all candidates
     */
    @Override
    public List getHiredCandidateList() {
        return hiredCandidateRepository.findAll();
    }

    /**
     * @param hiredCandidateDTO
     * @throws MessagingException
     * @throws LmsApiApplicationException
     */
    @Override
    public void save(HiredCandidateDTO hiredCandidateDTO) throws MessagingException, LmsApiApplicationException {
        HiredCandidate hiredCandidate = modelMapper.map(hiredCandidateDTO, HiredCandidate.class);
        if (hiredCandidate == null)
            throw new LmsApiApplicationException(LmsApiApplicationException.exceptionType.DATA_NOT_FOUND, "Data not found");
        hiredCandidateRepository.save(hiredCandidate);
        sendMail(hiredCandidate);
    }

    /**
     * @param id
     * @return Candidate data by id
     */
    @Override
    public HiredCandidate getCandidateProfile(long id) {
        return hiredCandidateRepository.findById(id).get();
    }

    /**
     * @param response
     * @param email
     * @return Updated candidate status
     * @throws LmsApiApplicationException
     */
    @Override
    public Response updateCandidateStatus(String response, String email) throws LmsApiApplicationException {
        HiredCandidate candidate = hiredCandidateRepository.findByEmail(email);
        if (candidate == null)
            throw new LmsApiApplicationException(LmsApiApplicationException.exceptionType.USER_NOT_FOUND, "User not found");
        candidate.setStatus(response);
        hiredCandidateRepository.save(candidate);
        return new Response(106, ApplicationConfiguration.getMessageAccessor().getMessage("106"));
    }

    /**
     * @param hiredCandidate
     * @throws MessagingException
     */
    @Override
    public void sendMail(HiredCandidate hiredCandidate) throws MessagingException {
        String Accepted = "<html><body><p><a href='http://localhost:8080/hiredcandidate/updatestatus?response=ACCEPTED&email=" + hiredCandidate.getEmail() + "'><img src='cid:identifier1234'></a></p></body></html>";
        String Rejected = "<html><body><p><a href='http://localhost:8080/hiredcandidate/updatestatus?response=REJECTED&email=" + hiredCandidate.getEmail() + "'><img src='cid:identifier5678'></a></p></body></html>";
        mailDTO.setTo(hiredCandidate.getEmail());
        mailDTO.setBody("Hii, " + hiredCandidate.getFirstName() + " " + hiredCandidate.getLastName() + " " +
                "You have been selected to our Fellowship Program. please click on the following " +
                "link to accept the offer. " + "\n" + Accepted + "\nPlease click on following link to reject the offer. " + "\n" + Rejected);
        mailDTO.setSubject("Fellowship Offer From BridgeLabz");
        mailDTO.setFrom("akftechnologies2@gmail.com");
        rabbitMQ.sendHiringMail(mailDTO);
    }

    /**
     * @return Response to user about job offer notification
     * @throws MessagingException
     */
    @Override
    public Response sendJobOfferNotification() throws MessagingException {
        List<HiredCandidate> acceptedCandidates = hiredCandidateRepository.findByStatus(Status.ACCEPTED.toString());
        for (HiredCandidate candidate : acceptedCandidates) {
            mailDTO.setTo(candidate.getEmail());
            mailDTO.setBody("Hii, " + candidate.getFirstName() + " " + candidate.getLastName() + " " +
                    "As per your confirmation, You have been officially admitted to BridgeLabz Fellowship" +
                    " Program." + "\n\n" + "We need you to update your personal information, " +
                    "your bank information and your educational information " +
                    " for our records." + "\n\n" + "Click on following links to do the same." +
                    "\n\n" + "\nFor Personal Information : " + "\n\n" +
                    personalInfo + "\n\n" +
                    "For Educational information : " + "\n\n" +
                    educationalInfo + "\n\n" +
                    "For Bank Information : " + "\n\n" +
                    bankInfo);
            mailDTO.setFrom("akftechnologies2@gmail.com");
            mailDTO.setSubject("Fellowship Job from BridgeLabz");
            rabbitMQ.sendJobOfferMail(mailDTO);
        }
        return new Response(102, ApplicationConfiguration.getMessageAccessor().getMessage("102"));
    }
}
