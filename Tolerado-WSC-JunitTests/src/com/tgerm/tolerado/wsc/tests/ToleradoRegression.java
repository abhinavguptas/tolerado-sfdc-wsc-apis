/*
Copyright (c) 2010 tgerm.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.tgerm.tolerado.wsc.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.tgerm.tolerado.wsc.tests.apex.ApexTest;
import com.tgerm.tolerado.wsc.tests.enterprise.AllOrNoneHeaderTest;
import com.tgerm.tolerado.wsc.tests.enterprise.EnterpriseContactTest;
import com.tgerm.tolerado.wsc.tests.enterprise.EnterpriseRetryableTest;
import com.tgerm.tolerado.wsc.tests.metadata.MetadataTest;
import com.tgerm.tolerado.wsc.tests.partner.PartnerContactTest;
import com.tgerm.tolerado.wsc.tests.partner.PartnerRetryableTest;
import com.tgerm.tolerado.wsc.tests.partner.sobject.ExtendingSObjectTest;
import com.tgerm.tolerado.wsc.tests.partner.sobject.SobjectUsageTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ EnterpriseContactTest.class, PartnerContactTest.class,
		MetadataTest.class, ApexTest.class, SobjectUsageTest.class,
		ExtendingSObjectTest.class, AllOrNoneHeaderTest.class,
		com.tgerm.tolerado.wsc.tests.partner.AllOrNoneHeaderTest.class,
		EnterpriseRetryableTest.class, PartnerRetryableTest.class })
public class ToleradoRegression {
}
