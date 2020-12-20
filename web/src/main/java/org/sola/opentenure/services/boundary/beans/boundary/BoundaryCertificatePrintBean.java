package org.sola.opentenure.services.boundary.beans.boundary;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.sola.common.StringUtility;
import org.sola.opentenure.services.boundary.beans.AbstractBackingBean;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import org.sola.common.ConfigConstants;
import org.sola.cs.services.ejb.system.businesslogic.SystemCSEJBLocal;
import org.sola.cs.services.ejbs.boundary.businesslogic.AdministrativeBoundaryMapImageEJBLocal;
import org.sola.opentenure.services.boundary.beans.report.ReportParam;
import org.sola.opentenure.services.boundary.beans.report.ReportServerBean;
import org.sola.opentenure.services.boundary.beans.report.ResourceDescription;
import org.sola.services.common.logging.LogUtility;

/**
 * Returns map image with boundary
 */
@Named
@ViewScoped
public class BoundaryCertificatePrintBean extends AbstractBackingBean {

    @EJB
    SystemCSEJBLocal systemEjb;
    
    @Inject
    ReportServerBean server;

    public void printBoundaryCertificate() {
        try {
            String id = getRequestParam("id");
             if(StringUtility.isEmpty(id)){
                return;
            }
               
            ResourceDescription report = server.getReport(systemEjb.getSetting(ConfigConstants.BOUNDARY_CERTIFICATE_URL, ""));
            List<ReportParam> params = server.getReportParameters(report.getUri());
            String communityName = systemEjb.getSetting(ConfigConstants.COMMUNITY_NAME, "Test community");
            
            if (params != null) {
                for (ReportParam param : params) {
                     if (param.getId().equalsIgnoreCase("id")) {
                         param.setValueString(id);
                     }
                    if (param.getId().equalsIgnoreCase("communityName")) {
                        param.setValueString(URLEncoder.encode(communityName, "UTF-8"));
                    }
                     if (param.getId().equalsIgnoreCase("imageUrl")) {
                        String appUrl = getApplicationUrl();
                        // JasperServer has issues with HTTPS protocol when generating output in PDF format. 
                        // So, try to replace https to http for workaround
                        appUrl = appUrl.replace("https:", "http:").replace(":8181", ":8080").replace(":443", "");
                        param.setValueString(URLEncoder.encode(appUrl + "/boundary/GetBoundaryMapImage.xhtml", "UTF-8"));
                     }
                }         
                server.runReport(report.getUri(), params.toArray(new ReportParam[params.size()]), "pdf");
 //             server.runReport(report.getUri(), params.toArray(new ReportParam[params.size()]), "html");
            }
        } catch (Exception e) {
            LogUtility.log("Failed to run boundary certificate printing", e);
        }
    }
}
