<p:declare-step version="1.0" type="px:ssml-to-audio" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:d="http://www.daisy.org/ns/pipeline/data">

  <p:input port="source" primary="true" sequence="true"/>
  <p:input port="config"/>
  <p:output port="result" primary="true" sequence="false"/>
  <p:option name="output-dir" select="''"/>

  <p:import href="synthesize.xpl" />

  <px:synthesize>
    <p:input port="config">
      <p:pipe port="config" step="main"/>
    </p:input>
    <p:with-option name="output-dir" select="$output-dir">
      <p:empty/>
    </p:with-option>
  </px:synthesize>

  <!-- uncomment those lines to rename the audio paths -->
  <!-- <p:for-each name="renaming"> -->
  <!--  <p:output port="result" primary="true" sequence="true"/> -->
  <!--   <p:iteration-source> -->
  <!--     <p:pipe port="result" step="synth"/> -->
  <!--   </p:iteration-source> -->
  <!--   <p:viewport match="//*[@src]" name="viewport"> -->
  <!--     <p:variable name="current-src" select="/*/@src"/> -->
  <!--     <p:string-replace match="@src"> -->
  <!-- 	<p:with-option name="replace" select="concat('&quot;', concat($audio-relative-dir, tokenize($current-src, '[\\/]+')[last()]),'&quot;')"/> -->
  <!--     </p:string-replace> -->
  <!--   </p:viewport> -->
  <!-- </p:for-each> -->

</p:declare-step>
