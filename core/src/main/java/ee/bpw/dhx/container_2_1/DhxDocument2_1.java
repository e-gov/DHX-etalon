package ee.bpw.dhx.container_2_1;

import java.io.File;
import java.io.InputStream;

import lombok.Getter;
import lombok.Setter;
import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxDocument;
import ee.bpw.dhx.model.XroadMember;
import ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import eu.x_road.dhx.producer.SendDocument;

@Getter
@Setter
public class DhxDocument2_1 extends DhxDocument{
	
	DecContainer container;
	
	public DhxDocument2_1(XroadMember service, DecContainer container, File file, Boolean packFile)throws DhxException{
		super(service, file, packFile);
		this.container = container;
		
	}
	
	public DhxDocument2_1(XroadMember service, InputStream stream, DecContainer container, Boolean packFile) throws DhxException{
		super(service, stream, packFile);
		this.container = container;
	}
	
	public DhxDocument2_1 (XroadMember client, SendDocument document, DecContainer container) {
		super(client, document);
		this.container = container;
	}

}
