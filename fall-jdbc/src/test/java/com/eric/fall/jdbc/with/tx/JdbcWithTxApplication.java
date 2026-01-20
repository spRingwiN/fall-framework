package com.eric.fall.jdbc.with.tx;

import com.eric.fall.annotation.ComponentScan;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.annotation.Import;
import com.eric.fall.jdbc.JdbcConfiguration;

@ComponentScan
@Configuration
@Import(JdbcConfiguration.class)
public class JdbcWithTxApplication {
}
