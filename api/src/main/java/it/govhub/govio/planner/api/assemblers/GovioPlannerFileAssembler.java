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
package it.govhub.govio.planner.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.planner.api.beans.GovioFile;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govio.planner.api.web.FileController;

@Component
public class GovioPlannerFileAssembler extends RepresentationModelAssemblerSupport<GovioPlannerFileEntity, GovioFile>{

	public GovioPlannerFileAssembler() {
		super(FileController.class, GovioFile.class);
	}

	
	@Override
	public GovioFile toModel(GovioPlannerFileEntity src) {
		
		GovioFile ret = new GovioFile();
		
		BeanUtils.copyProperties(src, ret);
		ret.setExpirationFileId(src.getExpirationFile().getId());
		ret.setFilename(src.getName());
		ret.add(
				linkTo(
					methodOn(FileController.class).readGovioFileInfo(src.getId()))
				.withSelfRel())
			.add(
				linkTo(
						methodOn(FileController.class).downloadGovioFile(src.getId()))
				.withRel("content"))
			.add(
					linkTo(
							methodOn(FileController.class).readExpirationsInfo(src.getExpirationFile().getId()))
					.withRel("expiration-file"));
		
		
		return ret;

	}

}
