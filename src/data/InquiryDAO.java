package data;

import models.Inquiry;
import utils.JsonUtil;
import java.util.*;

/**
 * Data Access Object for managing property inquiries.
 * Stores inquiries in-memory (for demo purposes).
 */
public class InquiryDAO {
    private List<Inquiry> inquiries;
    private static InquiryDAO instance;

    private InquiryDAO() {
        inquiries = new ArrayList<>();
    }

    public static synchronized InquiryDAO getInstance() {
        if (instance == null) {
            instance = new InquiryDAO();
        }
        return instance;
    }

    /**
     * Add a new inquiry
     */
    public Inquiry addInquiry(Map<String, Object> data) {
        Inquiry inquiry = Inquiry.fromMap(data);
        inquiries.add(inquiry);
        System.out.println("New inquiry received: " + inquiry.getName() + " for property " + inquiry.getPropertyId());
        return inquiry;
    }

    /**
     * Get all inquiries
     */
    public List<Inquiry> getAll() {
        return new ArrayList<>(inquiries);
    }

    /**
     * Get inquiries for a specific property
     */
    public List<Inquiry> getByPropertyId(String propertyId) {
        List<Inquiry> result = new ArrayList<>();
        for (Inquiry inq : inquiries) {
            if (inq.getPropertyId().equals(propertyId)) {
                result.add(inq);
            }
        }
        return result;
    }
}
