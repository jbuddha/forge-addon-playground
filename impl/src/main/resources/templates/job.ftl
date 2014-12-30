<job id="myjob" xmlns="http://xmlns.jcp.org/xml/ns/javaee" version="1.0">
    <step id="mystep">
        <chunk item-count="3">
            <reader ref="${readerBeanName}"/>
            <#if processorBeanName?? >
            <processor ref="${processorBeanName}"/>
            </#if>
            <writer ref="${writerBeanName}"/>
        </chunk>
    </step>
</job>