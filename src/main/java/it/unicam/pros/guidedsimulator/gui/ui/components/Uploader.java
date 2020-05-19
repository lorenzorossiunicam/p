package it.unicam.pros.guidedsimulator.gui.ui.components;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.InputStream;

public class Uploader {

    private InputStream stream;
    private Upload upload;

    public Uploader(String mimeType, String extension){
        MemoryBuffer buffer = new MemoryBuffer();
        this.upload = new Upload(buffer);
        upload.addSucceededListener(event -> {
            stream = createStream(event.getMIMEType(),
                    event.getFileName(), buffer.getInputStream(), mimeType, extension);
        });
    }

    public Upload getUploadComponent(){
        return upload;
    }

    public InputStream getStream(){
        return stream;
    }

    private InputStream createStream(String mimeType, String fileName, InputStream stream,String mime, String extension) {
        if (mimeType.startsWith(mime) && fileName.endsWith(extension)) {
            return stream;
        }
        return null;
    }
}
