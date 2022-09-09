# Installation Guide

## Helm Install of Driver

Prior to installing the driver, it may be necessary to:
- configure a secret containing trusted client certificates. See [Configuring Certificates](ConfiguringCertificates.md)


Download the Helm chart for the required version of the viptela-sdwan-driver. Run the following command to install the Helm chart with the default values

```bash
helm install viptela-sdwan-driver viptela-sdwan-driver-<version>.tgz
```

## Onboarding Driver into LM

Use lmctl for onboarding the driver into CP4NA. For full details on how to install or use lmctl, refer to its documentation.

The following command will onboard the viptela-sdwan-driver into CP4NA environment called 'dev01':

```bash
lmctl resourcedriver add --type viptela-sdwan --url http://viptela-sdwan-driver:8197 dev01
```

**NOTES**:
- The above example assumes lmctl has been configured with an environment called 'dev01'. Replace this environment name accordingly
- If this configuration doesn't include the password for the environment, one will be prompted for
