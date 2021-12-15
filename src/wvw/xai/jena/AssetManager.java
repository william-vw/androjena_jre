package wvw.xai.jena;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class AssetManager {

	public InputStream open(String path) throws IOException {
		return new FileInputStream(Paths.get(path).toFile());
	}
}
