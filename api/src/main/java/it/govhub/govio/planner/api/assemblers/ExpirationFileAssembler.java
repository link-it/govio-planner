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

import java.util.HashMap;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.planner.api.beans.ExpirationFile;
import it.govhub.govio.planner.api.beans.ExpirationFileEmbeds;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.web.FileController;
import it.govhub.govio.planner.api.web.UserController;
import it.govhub.govregistry.readops.api.assemblers.UserAssembler;

@Component
public class ExpirationFileAssembler extends RepresentationModelAssemblerSupport<ExpirationFileEntity, ExpirationFile> {

	@Autowired
	UserAssembler userAssembler;
	
	
	public ExpirationFileAssembler() {
		super(FileController.class, ExpirationFile.class);
	}

	
	@Override
	public ExpirationFile toModel(ExpirationFileEntity src) {
		
    	ExpirationFile ret = new ExpirationFile()
    			.creationDate(src.getCreationDate())
    			.processingDate(src.getProcessingDate())
    			.planId(src.getPlanId())
    			.filename(src.getName())
    			.uploaderId(src.getUploaderUser().getId())
    			.id(src.getId());
    			
		ret.add(
				linkTo(
					methodOn(FileController.class).readExpirationsInfo(src.getId()))
				.withSelfRel())
			.add(
				linkTo(
						methodOn(FileController.class).downloadExpirationsFile(src.getId()))
				.withRel("content"))
			.add(
					linkTo(
							methodOn(UserController.class).readUser(src.getUploaderUser().getId()))
					.withRel("uploader"));
    	
		return ret;
	}
	
	
	public ExpirationFile toEmbeddedModel(ExpirationFileEntity src, Set<ExpirationFileEmbeds> embeds) {
		var ret = this.toModel(src);
		
		if (!embeds.isEmpty()) {  
			ret.setEmbedded(new HashMap<>());
		}
			
		for (var embed : embeds) {
			if (embed.equals(ExpirationFileEmbeds.UPLOADER)) {
				ret.getEmbedded().put(ExpirationFileEmbeds.UPLOADER.getValue(), this.userAssembler.toModel(src.getUploaderUser())); 
			}
		}
		
		return ret;
	}
	
	
	public ExpirationFile toEmbeddedModel(ExpirationFileEntity src) {
		return this.toEmbeddedModel(src, Set.of(ExpirationFileEmbeds.values()));
	}
	
	
}
