package com.olivertechnology.letterservice.model;

import com.olivertechnology.letterservice.util.DBUtils;
import com.olivertechnology.letterservice.model.entity.MatterData;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class GetMergedData {

  public String matterUuid;
  public String template;

  public static String getMergedData(String rMatterUuid, String rTemplate) {
    log.info("Get Merged Data");
    log.info("rMatterUuid=" + rMatterUuid);
    log.info("rTemplate=" + rTemplate);

    // Get database connection
    DBUtils db = new DBUtils();

    String dataFields = db.getDataFields(rTemplate);
    log.info("dataFields=" + dataFields);

    MatterData md = db.getMatterData(rMatterUuid);

    StringBuilder jsonString = new StringBuilder("{ ");
    Boolean firstFlag = true;

    // For eliminating dupes
    Set<String> dc = new HashSet<String>();

    // Parse field names out of json data
    JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

    JSONArray dfs = null;
    try {
      dfs = (JSONArray) parser.parse(dataFields);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    for (Object df : dfs) {
      JSONObject obj = (JSONObject) df;
      String key = obj.getAsString("key");
      log.info("key=" + key);

      if (!dc.add(key)) {
        // Skip
        log.info("Skipping already added " + key);
      } else {
        //FIXME -- probably a better way to do this using a map or something
        String app = null;

        if (key.equals("venue_secondary_court_type")) {
          app = "\\\"venue_secondary_court_type\\\" : \\\"" + md.getVenueSecondaryCourtType() + "\\\"";
        } else if (key.equals("division_number")) {
          app = "\\\"division_number\\\" : \\\"" + md.getDivisionNumber() + "\\\"";
        } else if (key.equals("noa_letter_date")) {
          app = "\\\"noa_letter_date\\\" : \\\"" + md.getChargeOffDate() + "\\\"";
        } else if (key.equals("late_fees")) {
          app = "\\\"late_fees\\\" : \\\"" + md.getLateFees() + "\\\"";
        } else if (key.equals("balance_minus_late_fees")) {
          app = "\\\"balance_minus_late_fees\\\" : \\\"" + md.getBalanceMinusLateFees() + "\\\"";
        } else if (key.equals("filing_fees")) {
          app = "\\\"filing_fees\\\" : \\\"" + md.getFilingFees() + "\\\"";
        } else if (key.equals("service_fees")) {
          app = "\\\"service_fees\\\" : \\\"" + md.getServiceFees() + "\\\"";
        } else if (key.equals("total_prin_fees")) {
          app = "\\\"total_prin_fees\\\" : \\\"" + md.getTotalPrinFees() + "\\\"";
        } else if (key.equals("legal_case_number")) {
          app = "\\\"legal_case_number\\\" : \\\"" + md.getLegalCaseNumber() + "\\\"";
        } else if (key.equals("borrowers_name")) {
          app = "\\\"borrowers_name\\\" : \\\"" + md.getBorrowerName() + "\\\"";
        } else if (key.equals("coborrower_name")) {
          app = "\\\"coborrower_name\\\" : \\\"" + md.getCoborrowerName() + "\\\"";
        } else if (key.equals("creditor_name")) {
          app = "\\\"creditor_name\\\" : \\\"" + md.getCreditorName() + "\\\"";
        } else if (key.equals("account_number_redacted")) {
          app = "\\\"account_number_redacted\\\" : \\\"" + md.getAcctLastFour() + "\\\"";
        } else if (key.equals("product_description")) {
          app = "\\\"product_description\\\" : \\\"" + md.getProductDescription() + "\\\"";
        } else if (key.equals("current_balance")) {
          app = "\\\"current_balance\\\" : \\\"" + md.getCurrentBalance() + "\\\"";
        } else if (key.equals("venue_county")) {
          app = "\\\"venue_county\\\" : \\\"" + md.getVenueCounty() + "\\\"";
        } else if (key.equals("venue_primary_court_type")) {
          app = "\\\"venue_primary_court_type\\\" : \\\"" + md.getVenuePrimaryCourtType() + "\\\"";
        } else if (key.equals("open_date")) {
          app = "\\\"open_date\\\" : \\\"" + md.getOpenDate() + "\\\"";
        } else if (key.equals("firm_name")) {
          app = "\\\"firm_name\\\" : \\\"" + md.getFirmName() + "\\\"";
        } else if (key.equals("firm_address1")) {
          app = "\\\"firm_address1\\\" : \\\"" + md.getFirmAddress1() + "\\\"";
        } else if (key.equals("firm_address2")) {
          app = "\\\"firm_address2\\\" : \\\"" + md.getFirmAddress2() + "\\\"";
        } else if (key.equals("firm_csz")) {
          app = "\\\"firm_csz\\\" : \\\"" + md.getFirmCsz() + "\\\"";
        } else if (key.equals("firm_phone_number")) {
          app = "\\\"firm_phone_number\\\" : \\\"" + md.getFirmPhoneNumber() + "\\\"";
        } else if (key.equals("firm_fax_number")) {
          app = "\\\"firm_fax_number\\\" : \\\"" + md.getFirmFaxNumber() + "\\\"";
        } else if (key.equals("borrower_address1")) {
          app = "\\\"borrower_address1\\\" : \\\"" + md.getBorrowerAddress1() + "\\\"";
        } else if (key.equals("borrower_address2")) {
          app = "\\\"borrower_address2\\\" : \\\"" + md.getBorrowerAddress2() + "\\\"";
        } else if (key.equals("borrower_csz")) {
          app = "\\\"borrower_csz\\\" : \\\"" + md.getBorrowerCsz() + "\\\"";
        } else if (key.equals("coborrower_address1")) {
          app = "\\\"coborrower_address1\\\" : \\\"" + md.getCoborrowerAddress1() + "\\\"";
        } else if (key.equals("coborrower_address2")) {
          app = "\\\"coborrower_address2\\\" : \\\"" + md.getCoborrowerAddress2() + "\\\"";
        } else if (key.equals("coborrower_csz")) {
          app = "\\\"coborrower_csz\\\" : \\\"" + md.getCoborrowerCsz() + "\\\"";
        } else if (key.equals("charge_off_date")) {
          app = "\\\"noa_letter_date\\\" : \\\"" + md.getChargeOffDate() + "\\\"";
        } else if (key.equals("defendant_city")) {
          app = "\\\"defendant_city\\\" : \\\"" + md.getDefendentCity() + "\\\"";
        } else if (key.equals("borrower_aka")) {
          app = "\\\"borrower_aka\\\" : \\\"" + md.getBorrowerAka() + "\\\"";
        } else if (key.equals("coborrower_aka")) {
          app = "\\\"coborrower_aka\\\" : \\\"" + md.getCoborrowerAka() + "\\\"";
        } else if (key.equals("last_payment_date")) {
          app = "\\\"last_payment_date\\\" : \\\"" + md.getLastPaymentDate() + "\\\"";
        } else if (key.equals("last_payment_amount")) {
          app = "\\\"last_payment_amount\\\" : \\\"" + md.getLastPaymentAmount() + "\\\"";
        } else if (key.equals("firm_attorney_name1")) {
          app = "\\\"firm_attorney_name1\\\" : \\\"" + md.getFirmAttorneyName1() + "\\\"";
        } else if (key.equals("firm_attorney_name2")) {
          app = "\\\"firm_attorney_name2\\\" : \\\"" + md.getFirmAttorneyName2() + "\\\"";
        } else if (key.equals("firm_attorney_bar_number1")) {
          app = "\\\"firm_attorney_bar_number1\\\" : \\\"" + md.getFirmAttorneyBarNumber1() + "\\\"";
        } else if (key.equals("letter_date")) {
          app = "\\\"letter_date\\\" : \\\"" + md.getLetterDate() + "\\\"";
        } else if (key.equals("loan_specialist_name")) {
          app = "\\\"loan_specialist_name\\\" : \\\"" + md.getLoanSpecialistName() + "\\\"";
        } else if (key.equals("borrower_or_coborrower_name")) {
          String bcbn = md.getBorrowerName();
          if (bcbn == null || bcbn.equals(" ") || bcbn.isBlank() || bcbn.isEmpty()) {
            bcbn = md.getCoborrowerName();
          }
          app = "\\\"borrower_or_coborrower_name\\\" : \\\"" + bcbn + "\\\"";
        // Handle conditionals here
        } else if (key.equals("b_comma") || key.equals("c_comma")) {
          if (md.getCoborrowerName() != null || md.getCoborrowerName().equals(" ") || md.getCoborrowerName().isBlank() || md.getCoborrowerName().isEmpty()) {
            app = ", ";
          } else {
            app = "";
          }
        } else if (key.equals("b_semi")) {
          if (md.getCoborrowerName() != null || md.getCoborrowerName().equals(" ") || md.getCoborrowerName().isBlank() || md.getCoborrowerName().isEmpty()) {
            app = ";";
          } else {
            app = "";
          }
        } else if (key.equals("c_semi")) {
          if (md.getCoborrowerName() != null || md.getCoborrowerName().equals(" ") || md.getCoborrowerName().isBlank() || md.getCoborrowerName().isEmpty()) {
            app = ";";
          } else {
            app = "";
          }
        } else if (key.equals("c_and")) {
          if (md.getCoborrowerName() != null || md.getCoborrowerName().equals(" ") || md.getCoborrowerName().isBlank() || md.getCoborrowerName().isEmpty()) {
            app = " and ";
          } else {
            app = "";
          }
        }

        if (app != null) {
          if (firstFlag) {
            firstFlag = false;
          } else {
            jsonString.append(", ");
          }
          jsonString.append(app);
        } else {
          log.error("key not found " + key);
        }
      }
    }
    jsonString.append(" }");

    log.info("jsonString=" + jsonString.toString());

    return jsonString.toString();
  }
}

