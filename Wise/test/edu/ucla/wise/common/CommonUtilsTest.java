package edu.ucla.wise.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.ucla.wise.commons.CommonUtils;

public class CommonUtilsTest {
	
	@Test
	public void encodeDecodeTest(){
		assertEquals(CommonUtils.base64Encode("wisedev"),"d2lzZWRldg==");
		assertEquals(CommonUtils.base64Decode("d2lzZWRldg=="),"wisedev");
	}
}
