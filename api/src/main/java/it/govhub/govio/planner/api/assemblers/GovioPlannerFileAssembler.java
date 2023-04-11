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
