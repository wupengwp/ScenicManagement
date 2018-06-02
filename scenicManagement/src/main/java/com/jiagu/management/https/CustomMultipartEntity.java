package com.jiagu.management.https;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

/** 
* @ClassName: CustomMultipartEntity 
* @Description: 
* @author zz
* @date 2015年1月15日 下午12:34:55 
* @version 1
* @company 西安甲骨企业文化传播有限公司  
*/  
public class CustomMultipartEntity extends MultipartEntity {

	private final ProgressListener listener;

	public CustomMultipartEntity(final ProgressListener listener) {
//		super();
		this.listener = listener;
	}

	public CustomMultipartEntity(final HttpMultipartMode mode,
			final ProgressListener listener) {
//		super(mode);
		this.listener = listener;
	}

	public CustomMultipartEntity(HttpMultipartMode mode, final String boundary,
			final Charset charset, final ProgressListener listener) {
//		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener));
	}

	public static interface ProgressListener {
		void transferred(long num);
	}

	public static class CountingOutputStream extends FilterOutputStream {

		private final ProgressListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out,
				final ProgressListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			this.listener.transferred(this.transferred);
		}

		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
			this.listener.transferred(this.transferred);
		}
	}

}