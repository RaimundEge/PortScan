# PortScan
3 components:
## PortScanServer
    runs during CSCI350
    opens random UPD and TCP ports 9000 - 9100
    records connections in mongodb "csci350' collection "logrecords"
    run via service PortScanServer
## PortScanLogRest
    moved to BlitzDockerCompose
    express server to provide logrecords to PortScanLogVue
## PortScanLogVue
    vue.js website to display logrecords
