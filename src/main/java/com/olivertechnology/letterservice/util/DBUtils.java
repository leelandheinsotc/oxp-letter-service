package com.olivertechnology.letterservice.util;

import com.olivertechnology.letterservice.model.entity.MatterData;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DBUtils {

  static String oxpDbSecretName;
  static String adtDbSecretName;
  static String matterSecretName;
  static String secretRegion;
  @Autowired
  Environment env;

  @Value("${db.oxpDbSecret}")
  public void setOxpDbSecret(String oxpDbSecretName) {
    DBUtils.oxpDbSecretName = oxpDbSecretName;
  }

  @Value("${db.adtDbSecret}")
  public void setAdtDbSecret(String adtDbSecretName) {
    DBUtils.adtDbSecretName = adtDbSecretName;
  }
  @Value("${db.matterSecret}")
  public void setMatterSecret(String matterSecretName) {
    DBUtils.matterSecretName = matterSecretName;
  }

  @Value("${db.dbRegion}")
  public void setRegion(String secretRegion) {
    DBUtils.secretRegion = secretRegion;
  }

  public String getDataFields(String key) {
    log.info("Fetching Data Fields");
    log.info("key=" + key);

    String dataFields = null;

    Connection c = null;
    Statement stmt = null;

    //System.out.println("oxpDbSecretName=" + oxpDbSecretName);
    //System.out.println("secretRegion=" + secretRegion);
    DBDetails details = getDBDetails(oxpDbSecretName);

    ResultSet rsdm = null;

    try {
      Class.forName("org.postgresql.Driver");

      c = DriverManager.getConnection(details.getUrl(), details.getUsername(),
          details.getPassword());

      log.debug("Successfully Connected to operations db to get Data Mapping.");
      stmt = c.createStatement();

      String sqlStatement =
          "SELECT data_fields " + "FROM templates " + "WHERE template_reference = '" + key
              + "' AND is_active = true";
      rsdm = stmt.executeQuery(sqlStatement);
      while (rsdm.next()) {
        dataFields = rsdm.getString("data_fields");
      }
    } catch (Exception e) {
      log.error("getDataFields error: {}", e);
    } finally {
      try {
        if (rsdm != null) {
          rsdm.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (c != null) {
          c.close();
        }
      } catch (SQLException e) {
        log.error("getDataFields SQL error: {}", e);
        throw new RuntimeException(e);
      }
    }

    return dataFields;
  }

  public MatterData getMatterData(String matterUuid) {
    log.info("getMatterData: " + matterUuid);

    MatterData matterData = new MatterData();

    Connection conn = null;
    Statement stmt = null;

    DBDetails matterDetails = getDBDetails(matterSecretName);

    ResultSet mRsdm = null;
    ResultSet bRsdm = null;
    ResultSet cRsdm = null;
    ResultSet pRsdm = null;
    ResultSet aaRsdm = null;

    try {
      Class.forName("org.postgresql.Driver");

      conn = DriverManager.getConnection(matterDetails.getUrl(), matterDetails.getUsername(),
              matterDetails.getPassword());

      log.debug("Successfully Connected to operations db to get matter data.");
      stmt = conn.createStatement();

      // Get accounts data
      String accountSql = "SELECT * FROM accounts WHERE matter_uuid = '" + matterUuid + "'";
      log.info("accountSql=" + accountSql);
      ResultSet aRsdm = stmt.executeQuery(accountSql);
      String accountUuid = "";
      String accountNumber = "";
      String currentBalance = "";
      String lastPaymentDate = "";
      String lastPaymentAmount = "";
      String chargeOffDate = "";
      String chargeOffAmount = "";
      String openDate = "";
      while (aRsdm.next()) {
        accountUuid = aRsdm.getString("account_uuid");
        accountNumber = aRsdm.getString("account_number");
        currentBalance = aRsdm.getString("current_debt_balance");
        lastPaymentDate = aRsdm.getString("last_payment_effective_date");
        lastPaymentAmount = aRsdm.getString("last_payment_amount");
        chargeOffDate = aRsdm.getString("charge_off_date");
        chargeOffAmount = aRsdm.getString("charge_off_amount");
        openDate = aRsdm.getString("open_date");
      }
      log.info("accountUuid=" + accountUuid);
      log.info("accountNumber=" + accountNumber);
      log.info("currentBalance=" + currentBalance);
      log.info("lastPaymentDate=" + lastPaymentDate);
      log.info("lastPaymentAmount=" + lastPaymentAmount);
      log.info("chargeOffDate=" + chargeOffDate);
      log.info("chargeOffAmount=" + chargeOffAmount);

      String acctLastFour = null;
      if (accountNumber.length() > 4) {
        accountNumber.substring(accountNumber.length() - 4);
      }

      matterData.setAcctLastFour(acctLastFour);
      matterData.setCurrentBalance(currentBalance);
      matterData.setLastPaymentDate(lastPaymentDate);
      matterData.setLastPaymentAmount(lastPaymentAmount);
      matterData.setChargeOffDate(chargeOffDate);
      matterData.setChargeOffAmount(chargeOffAmount);
      matterData.setOpenDate(openDate);

      // Get matter data
      String mattersSql = "SELECT current_placement_uuid FROM matters WHERE matter_uuid = '" + matterUuid + "'";
      log.info("mattersSql=" + mattersSql);
      mRsdm = stmt.executeQuery(mattersSql);
      String attorneyUuid = "";
      while (mRsdm.next()) {
        attorneyUuid = mRsdm.getString("current_placement_uuid");
      }
      log.info("attorneyUuid=" + attorneyUuid);

      // Get borrower name and address data.
      String borrowerSql = "SELECT a.*, c.* "
              + "FROM addresses a, customers c "
              + "WHERE c.matter_uuid = '" + matterUuid + "' "
              + "AND c.customer_type = 'Borrower' "
              + "AND (a.address_uuid = c.mailing_address_uuid OR a.address_uuid = c.official_address_uuid)";
      log.info("borrowerSql=" + borrowerSql);
      String borrowerName = "";
      String borrowerAddress1 = "";
      String borrowerAddress2 = "";
      String borrowerCsz = "";
      String borrowerAka = "";

      String borrowerFirstName = null;
      String borrowerMiddleName = null;
      String borrowerLastName = null;
      String tAddressLine2 = null;
      String city = null;
      String stateProvCode = null;
      String postalCode = null;
      String postalCodeSegment = null;
      bRsdm = stmt.executeQuery(borrowerSql);
      while (bRsdm.next()) {
        borrowerFirstName = bRsdm.getString("first_name");
        borrowerLastName = bRsdm.getString("last_name");
        borrowerMiddleName = bRsdm.getString("middle_name");
        borrowerName = borrowerFirstName + " " + borrowerMiddleName + " " + borrowerLastName;
        borrowerAddress1 = bRsdm.getString("address_line_1");
        tAddressLine2 = bRsdm.getString("address_line_2");
        if (tAddressLine2 == null) {
          borrowerAddress2 = "";
        } else {
          borrowerAddress2 = tAddressLine2;
        }
        city = bRsdm.getString("city");
        stateProvCode = bRsdm.getString("state_prov_code");
        postalCode = bRsdm.getString("postal_code");
        postalCodeSegment = bRsdm.getString("postal_code_segment");
        borrowerCsz = city + ", " + stateProvCode + " " + postalCode;
      }

      matterData.setBorrowerName(borrowerName);
      matterData.setBorrowerAddress1(borrowerAddress1);
      matterData.setBorrowerAddress2(borrowerAddress2);
      matterData.setBorrowerCsz(borrowerCsz);

      // Get coborrower data
      String coborrowerSql = "SELECT a.*, c.* "
              + "FROM addresses a, customers c "
              + "WHERE c.matter_uuid = '" + matterUuid + "' "
              + "AND c.customer_type = 'Coborrower' "
              + "AND (a.address_uuid = c.mailing_address_uuid OR a.address_uuid = c.official_address_uuid)";
      log.info("coborrowerSql=" + coborrowerSql);
      String coborrowerName = "";
      String coborrowerFirstName = "";
      String coborrowerMiddleName = "";
      String coborrowerLastName = "";
      String coborrowerAddress1 = "";
      String coborrowerAddress2 = "";
      String coborrowerCsz = "";
      String defendentCity = "";
      String coborrowerAka = "";
      cRsdm = stmt.executeQuery(borrowerSql);
      while (cRsdm.next()) {
        coborrowerFirstName = cRsdm.getString("first_name");
        coborrowerLastName = cRsdm.getString("last_name");
        coborrowerMiddleName = cRsdm.getString("middle_name");
        coborrowerName = coborrowerFirstName + " " + coborrowerMiddleName + " " + coborrowerLastName;
        coborrowerAddress1 = cRsdm.getString("address_line_1");
        tAddressLine2 = cRsdm.getString("address_line_2");
        if (tAddressLine2 == null) {
          coborrowerAddress2 = "";
        } else {
          coborrowerAddress2 = tAddressLine2;
        }
        city = cRsdm.getString("city");
        stateProvCode = cRsdm.getString("state_prov_code");
        postalCode = cRsdm.getString("postal_code");
        postalCodeSegment = cRsdm.getString("postal_code_segment");
        coborrowerCsz = city + ", " + stateProvCode + " " + postalCode;
      }

      matterData.setCoborrowerName(coborrowerName);
      matterData.setCoborrowerAddress1(coborrowerAddress1);
      matterData.setCoborrowerAddress2(coborrowerAddress2);
      matterData.setCoborrowerCsz(coborrowerCsz);
      matterData.setCoborrowerAka(coborrowerAka);

      //FIXME -- Get this info where to find?
      String filingFees = "";
      String serviceFees = "";
      String totalPrinFees = "";
      String legalCaseNumber = "";
      String creditorName = "";
      String productDescription = "";
      String venueCounty = "";
      String venuePrimaryCourtType = "";
      matterData.setFilingFees(filingFees);
      matterData.setServiceFees(serviceFees);
      matterData.setTotalPrinFees(totalPrinFees);
      matterData.setLegalCaseNumber(legalCaseNumber);
      matterData.setCreditorName(creditorName);
      matterData.setProductDescription(productDescription);
      matterData.setVenueCounty(venueCounty);
      matterData.setVenuePrimaryCourtType(venuePrimaryCourtType);

      // Get attorneys information here
      String firmName = "";
      String firmAddress1 = "";
      String firmAddress2 = "";
      String firmCsz = "";
      String firmPhoneNumber = "";
      String firmFaxNumber = "";
      String firmAttorneyName1 = "";
      String firmAttorneyName2 = "";
      String firmAttorneyBarNumber1 = "";
      // Get Placemment info Note: No is_active on this table?
      String placeSql = "SELECT service_firm_uuid FROM placements WHERE matter_uuid = '" + matterUuid + "'";
      log.info("placeSql=" + placeSql);
      String serviceFirmUuid = "";
      pRsdm = stmt.executeQuery(placeSql);
      while (pRsdm.next()) {
        serviceFirmUuid = pRsdm.getString("service_firm_uuid");
      }
      log.info("serviceFirmUuid=" + serviceFirmUuid);

      if (serviceFirmUuid != null && !serviceFirmUuid.isBlank() && !serviceFirmUuid.isEmpty()) {
        // Get Attorney info Note: No is_active on this table?
        String attySql = "SELECT * FROM service_firm WHERE service_firm_uuid = '" + serviceFirmUuid + "'";
        log.info("attySql=" + attySql);
        ResultSet atRsdm = stmt.executeQuery(placeSql);
        String attyAddressUuid = "";
        while (atRsdm.next()) {
          firmName = atRsdm.getString("firm_name");
          firmPhoneNumber = atRsdm.getString("phone");
          firmFaxNumber = atRsdm.getString("fax");
          attyAddressUuid = atRsdm.getString("mailing_adders_uuid");
        }
        log.info("firmName=" + firmName);
        log.info("firmPhoneNumber=" + firmPhoneNumber);
        log.info("firmFaxNumber=" + firmFaxNumber);
        log.info("fattyAddressUuid=" + attyAddressUuid);

        matterData.setFirmName(firmName);
        matterData.setFirmPhoneNumber(firmPhoneNumber);
        matterData.setFirmFaxNumber(firmFaxNumber);

        if (attyAddressUuid != null && !attyAddressUuid.isBlank() && !attyAddressUuid.isEmpty()) {
          // Get attorney address data
          String aaSql = "SELECT * "
                  + "FROM addresses "
                  + "WHERE address_uuid = '" + attyAddressUuid + "'";
          log.info("aaSql=" + aaSql);

          aaRsdm = stmt.executeQuery(aaSql);
          while (aaRsdm.next()) {
            firmAddress1 = aaRsdm.getString("address_line_1");
            tAddressLine2 = aaRsdm.getString("address_line_2");
            if (tAddressLine2 == null) {
              firmAddress2 = "";
            } else {
              firmAddress2 = tAddressLine2;
            }
            city = aaRsdm.getString("city");
            stateProvCode = aaRsdm.getString("state_prov_code");
            postalCode = aaRsdm.getString("postal_code");
            postalCodeSegment = aaRsdm.getString("postal_code_segment");
            firmCsz = city + ", " + stateProvCode + " " + postalCode;
          }

          log.info("firmAddress1=" + firmAddress1);
          log.info("firmAddress2=" + firmAddress2);
          log.info("firmCsz=" + firmCsz);
          log.info("firmAttorneyName1=" + firmAttorneyName1);
          log.info("firmAttorneyName2=" + firmAttorneyName2);
          log.info("firmAttorneyBarNumber1=" + firmAttorneyBarNumber1);

          matterData.setFirmAddress1(firmAddress1);
          matterData.setFirmAddress2(firmAddress2);
          matterData.setFirmCsz(firmCsz);
          matterData.setFirmAttorneyName1(firmAttorneyName1);
          matterData.setFirmAttorneyName2(firmAttorneyName2);
          matterData.setFirmAttorneyBarNumber1(firmAttorneyBarNumber1);
        }
      } else {
        log.info("No attorney found");
      }

      // Fill these in.
      DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
      LocalDate today = LocalDate.now();
      log.info("today=" + today.toString());
      matterData.setLetterDate(today.toString());

      //FIXME -- where to get this?
      String loanSpecialistName = "";
      matterData.setLoanSpecialistName(loanSpecialistName);
    } catch (Exception e) {
      log.error("getMatterData error: ", e);
    } finally {
      try {
        if (mRsdm != null) {
          mRsdm.close();
        }
        if (bRsdm != null) {
          bRsdm.close();
        }
        if (cRsdm != null) {
          cRsdm.close();
        }
        if (pRsdm != null) {
          pRsdm.close();
        }
        if (aaRsdm != null) {
          aaRsdm.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.error("getMatterData SQL error: ", e);
        throw new RuntimeException(e);
      }
    }

    return matterData;
  }

  public String findEvent(String matterUuid, String eventName) {
    log.info("findEvent: matterUuid=" + matterUuid);
    log.info("findEvent: eventName=" + eventName);

    Connection conn = null;
    Statement stmt = null;

    DBDetails matterDetails = getDBDetails(matterSecretName);

    String eventUuid = null;

    ResultSet eRsdm = null;

    try {
      Class.forName("org.postgresql.Driver");

      conn = DriverManager.getConnection(matterDetails.getUrl(), matterDetails.getUsername(),
              matterDetails.getPassword());

      log.debug("Successfully Connected to operations db to find events.");
      stmt = conn.createStatement();

      //FIXME -- waiting for real details on how to query events table
      String eventSql = "SELECT e.event_uuid FROM events e, event_configurations ec WHERE e.matter_uuid = '" + matterUuid + "'"
            + " AND e.event_configuration_uuid = ec.event_configuration_uuid"
            + " AND ec.name = 'CHARGE_OFF'";
      eRsdm = stmt.executeQuery(eventSql);

      while (eRsdm.next()) {
        eventUuid = eRsdm.getString("event_uuid");
      }
      log.info("eventUuid=" + eventUuid);
    } catch (Exception e) {
      log.error("findEvent error: {}", e);
    } finally {
      try {
        if (eRsdm != null) {
          eRsdm.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.error("findEvent SQL error: {}", e);
        throw new RuntimeException(e);
      }
    }

    return eventUuid;
  }

  public String getFileUuid(String matterUuid, String templateReference) {
    log.info("getFileUuid: matterid=" + matterUuid);
    log.info("getFileUuid: templateReference=" + templateReference);

    Connection conn = null;
    Statement stmt = null;

    DBDetails oxpDetails = getDBDetails(oxpDbSecretName);

    String fileUuid = null;

    ResultSet fRsdm = null;

    try {
      Class.forName("org.postgresql.Driver");

      conn = DriverManager.getConnection(oxpDetails.getUrl(), oxpDetails.getUsername(),
              oxpDetails.getPassword());

      log.debug("Successfully Connected to operations db to get file uuid.");
      stmt = conn.createStatement();

      String fileSql = "SELECT file_uuid FROM files WHERE matter_id = '" + matterUuid + "' "
              + "AND key = 'matters/" + matterUuid + "/" + templateReference + "'";
      log.info("fileSql=" + fileSql);

      fRsdm = stmt.executeQuery(fileSql);
      while (fRsdm.next()) {
        fileUuid = fRsdm.getString("file_uuid");
      }
      log.info("fileUuid=" + fileUuid);
    } catch (Exception e) {
      log.error("getFileUuid error: {}", e);
    } finally {
      try {
        if (fRsdm != null) {
          fRsdm.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.error("getFileUuid SQL error: {}", e);
        throw new RuntimeException(e);
      }
    }

    return fileUuid;
  }

  public DBDetails getDBDetails(String oxpDbSecretName) {
    DBDetails details = new DBDetails();

    //System.out.println("secretRegion=" + secretRegion);
    //System.out.println("oxpDbSecretName=" + oxpDbSecretName);

    JSONObject secretObj = SecretManager.getSecret(oxpDbSecretName, secretRegion);
    String username = secretObj.getAsString("username");
    String password = secretObj.getAsString("password");
    String host = secretObj.getAsString("host");
    String dbname = secretObj.getAsString("dbname");

    //System.out.println("username=" + username);
    //System.out.println("password=" + password);
    //System.out.println("host=" + host);
    //System.out.println("dbname=" + dbname);

    String url = "jdbc:postgresql://" + host + "/" + dbname;

    details.setUrl(url);
    details.setUsername(username);
    details.setPassword(password);

    return details;
  }
}

