package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 共通関数ヘルパー
 *
 */
public interface CommonFunction {
	/**
	 * inputを分岐し、新しいInputStreamオブジェクトを返す。読み込んだ内容をoutputにも出力する。
	 * 目的、InputStream内容を損失しないくログする。
	 * 
	 * @param output	出力するOutputStream
	 * @param input		元のInputStream
	 * @return			新InputStream
	 * @throws IOException
	 */
	default InputStream duplicateInputStream(OutputStream output, InputStream input) throws IOException {
		ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		
		while((len = input.read(buffer)) > 0) {
			bufferStream.write(buffer, 0, len);
			output.write(buffer, 0, len);
		}
		
		bufferStream.flush();
		output.flush();
		
		InputStream rst = new ByteArrayInputStream(bufferStream.toByteArray());
		bufferStream.close();
		
		return rst;
	}

	/**
	 * 入力ストリームから内容をバッファに入れる。
	 * 
	 * @param stream
	 *            入力ストレーム
	 * @return バッファ
	 */
	default byte[] dumpStream(InputStream stream) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len;
			byte[] buf = new byte[1024];

			while ((len = stream.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}

			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
