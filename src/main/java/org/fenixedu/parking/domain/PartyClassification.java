/**
 * Copyright © 2002 Instituto Superior Técnico
 *
 * This file is part of FenixEdu Core.
 *
 * FenixEdu Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu Core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.parking.domain;

import net.sourceforge.fenixedu.domain.ExecutionSemester;
import net.sourceforge.fenixedu.domain.Person;
import net.sourceforge.fenixedu.domain.Teacher;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.organizationalStructure.Party;
import net.sourceforge.fenixedu.domain.organizationalStructure.Unit;
import net.sourceforge.fenixedu.domain.person.RoleType;
import net.sourceforge.fenixedu.domain.personnelSection.contracts.PersonContractSituation;
import net.sourceforge.fenixedu.domain.teacher.CategoryType;

public enum PartyClassification {
    TEACHER, EMPLOYEE, RESEARCHER, GRANT_OWNER, MASTER_DEGREE, DEGREE, BOLONHA_SPECIALIZATION_DEGREE,
    BOLONHA_ADVANCED_FORMATION_DIPLOMA, BOLONHA_MASTER_DEGREE, BOLONHA_INTEGRATED_MASTER_DEGREE,
    BOLONHA_ADVANCED_SPECIALIZATION_DIPLOMA, BOLONHA_DEGREE, PERSON, UNIT;

    public static PartyClassification getClassificationByDegreeType(DegreeType degreeType) {
        return valueOf(degreeType.name());
    }

    public static PartyClassification getPartyClassification(Party party) {
        if (party instanceof Unit) {
            return PartyClassification.UNIT;
        }
        if (party instanceof Person) {
            Person person = (Person) party;
            final Teacher teacher = person.getTeacher();
            if (teacher != null) {
                final ExecutionSemester actualExecutionSemester = ExecutionSemester.readActualExecutionSemester();
                if (!teacher.isInactive(actualExecutionSemester) && !teacher.isMonitor(actualExecutionSemester)) {
                    return PartyClassification.TEACHER;
                }
            }
            if (person.getEmployee() != null && person.getEmployee().isActive()) {
                return PartyClassification.EMPLOYEE;
            }
            if (person.getPersonRole(RoleType.GRANT_OWNER) != null && person.getEmployee() != null) {
                final PersonContractSituation currentGrantOwnerContractSituation =
                        person.getPersonProfessionalData() != null ? person.getPersonProfessionalData()
                                .getCurrentPersonContractSituationByCategoryType(CategoryType.GRANT_OWNER) : null;
                if (currentGrantOwnerContractSituation != null) {
                    return PartyClassification.GRANT_OWNER;
                }
            }
            if (person.getResearcher() != null && person.getResearcher().isActiveContractedResearcher()) {
                return PartyClassification.RESEARCHER;
            }
            if (person.getStudent() != null) {
                final DegreeType degreeType = person.getStudent().getMostSignificantDegreeType();
                if (degreeType != null) {
                    return PartyClassification.getClassificationByDegreeType(degreeType);
                }
            }
            return PartyClassification.PERSON;
        }
        return PartyClassification.UNIT;
    }

    public static Integer getMostSignificantNumber(Person person) {
        PartyClassification classification = getPartyClassification(person);
        if (classification.equals(PartyClassification.TEACHER)) {
            if (person.getEmployee() != null) {
                return person.getEmployee().getEmployeeNumber();
            }
        }
        if (classification.equals(PartyClassification.EMPLOYEE)) {
            return person.getEmployee().getEmployeeNumber();
        }
        if (classification.equals(PartyClassification.RESEARCHER) && person.getEmployee() != null) {
            return person.getEmployee().getEmployeeNumber();
        }
        if (person.getStudent() != null) {
            return person.getStudent().getNumber();
        }
        if (classification.equals(PartyClassification.GRANT_OWNER) && person.getEmployee() != null) {
            return person.getEmployee().getEmployeeNumber();
        }
        return 0;
    }

}
