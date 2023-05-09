/*******************************************************************************
 *  GovIO Planner - Notification system Planner for AppIO
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package it.govhub.govio.planner.batch.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "govio_planner_ntfy_files")
public class GovioFileProducedEntity {

        public enum Status {CREATED, PROCESSING, PROCESSED}
        
        @Id 
        @SequenceGenerator(name="seq_govio_planner_govio_files",sequenceName="seq_govio_planner_govio_files", initialValue=1, allocationSize=1)
        @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_planner_govio_files")
        private Long id;
        
        @Column(name = "creation_date", nullable = false)
        private OffsetDateTime creationDate;
        
        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        private Status status;
        
        @Column(name = "location", nullable = false)
        private String location;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_govio_planner_file", nullable = false, foreignKey = @ForeignKey(name = "GovioFileProducedEntity_GovioPlannerFile"))
        private ExpirationCIEFileEntity expirationFile;
}
