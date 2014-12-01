/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of Fenix Parking.
 *
 * Fenix Parking is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fenix Parking is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fenix Parking.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.parking.ui.Action.externalServices;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.service.services.exceptions.FenixServiceException;
import org.fenixedu.academic.ui.struts.action.base.FenixDispatchAction;
import org.fenixedu.bennu.struts.annotations.Mapping;
import org.fenixedu.idcards.domain.SantanderEntry;
import org.fenixedu.parking.ParkingConfigurationManager;
import org.fenixedu.parking.domain.ParkingParty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;

@Mapping(path = "/setParkingCardId", module = "external")
public class SetParkingCardIdDA extends FenixDispatchAction {

    private static final Logger logger = LoggerFactory.getLogger(SetParkingCardIdDA.class);

    public ActionForward setSantanderId(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final String password = request.getParameter("password");
        final String identificationCardCode = request.getParameter("identificationCardCode");
        final Long parkingCardID = Long.valueOf(request.getParameter("parkingCardId"));
        final String categoryCode = request.getParameter("catCode");

        String message = "ko";

        try {
            message = runSantander(password, categoryCode, identificationCardCode, parkingCardID);
        } catch (NotAuthorizedException ex) {
            message = "Not authorized";
        } catch (UserDoesNotExistException ex) {
            message = "User does not exist.";
        } catch (Throwable ex) {
            message = ex.getMessage();
            logger.error(ex.getMessage(), ex);
        } finally {
            final ServletOutputStream servletOutputStream = response.getOutputStream();
            response.setContentType("text/html");
            servletOutputStream.print(message);
            servletOutputStream.flush();
            response.flushBuffer();
        }

        return null;
    }

    public static class NotAuthorizedException extends FenixServiceException {
    }

    public static class UserDoesNotExistException extends FenixServiceException {

    }

    private static final String password;
    static {
        password = ParkingConfigurationManager.getConfiguration().getParkingCardIdAdminPassword();
    }

    private static boolean isAllowed(final String password) {
        return SetParkingCardIdDA.password != null && SetParkingCardIdDA.password.equals(password);
    }

    @Atomic
    private static String runSantander(final String password, final String categoryCode, final String identificationCardCode,
            final Long parkingCardID) throws FenixServiceException {
        if (isAllowed(password)) {
            SantanderEntry entry = SantanderEntry.readByUsernameAndCategory(identificationCardCode, categoryCode);
            if (entry == null) {
                throw new UserDoesNotExistException();
            }
            if (entry.getPerson().getParkingParty() == null) {
                createParkingParty(entry.getPerson());
            }
            entry.getPerson().getParkingParty().setCardNumber(parkingCardID);
            return entry.getPerson().getUsername();
        } else {
            throw new NotAuthorizedException();
        }
    }

    @Atomic
    private static ParkingParty createParkingParty(Party party) {
        return new ParkingParty(party);
    }

}