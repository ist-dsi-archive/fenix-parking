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
package org.fenixedu.parking.domain;

import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.Teacher;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.organizationalStructure.Party;
import org.fenixedu.academic.domain.organizationalStructure.Unit;

import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonContractSituation;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.PersonProfessionalData;
import pt.ist.fenixedu.contracts.domain.personnelSection.contracts.ProfessionalCategory;
import pt.ist.fenixedu.contracts.domain.util.CategoryType;

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
                if (!PersonProfessionalData.isTeacherInactive(teacher, actualExecutionSemester)
                        && !ProfessionalCategory.isMonitor(teacher, actualExecutionSemester)) {
                    return PartyClassification.TEACHER;
                }
            }
            if (person.getEmployee() != null && person.getEmployee().isActive()) {
                return PartyClassification.EMPLOYEE;
            }
            if (person.getEmployee() != null) {
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
