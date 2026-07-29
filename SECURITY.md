# Security Policy

## Supported Versions

PadConnectReceiver is under active development. Only the latest released version is supported with security fixes. Please make sure you're on the most recent release before reporting an issue.

| Version | Supported |
| ------- | --------- |
| Latest release | ✅ |
| Older releases | ❌ |

## Reporting a Vulnerability

If you discover a security vulnerability in PadConnectReceiver, please **do not open a public GitHub issue**. Instead:

1. Report it privately via **GitHub's [private vulnerability reporting](https://github.com/Ishan09811/PadConnectReceiver/security/advisories/new)** feature on this repository (Security tab -> "Report a vulnerability"), or
2. Reach out on the [Discord server](https://discord.gg/BrMAZbEyXs) and request a private channel with a maintainer.

Please include:
- A description of the vulnerability and its potential impact
- Steps to reproduce (proof-of-concept if possible)
- Affected version(s) of PadConnectReceiver and, if relevant, [PadConnect](https://github.com/Ishan09811/PadConnect)
- Your OS version, if using windows os then ViGEmBus version, and receiver build, if relevant to reproduction

You should receive an initial response within a few days. This is a small open-source project maintained in spare time, so please be patient, response and fix timelines aren't guaranteed, but reports are taken seriously.

## Scope and Known Risk Areas

PadConnectReceiver listens for **untrusted UDP input on the local network** and executes it to emulate a virtual controller. This makes certain areas particularly security sensitive:

- **UDP packet parsing** - malformed, malicious, or unexpected packets sent to the receiver's listening port should be handled safely (no crashes, memory corruption, or unexpected native behaviour). This is the primary attack surface and the highest-priority area for security review.
- **No built in encryption or authentication on the input stream by default** so anyone on the same local network can potentially send UDP packets to the receiver's port, not just a legitimate [PadConnect](https://github.com/Ishan09811/PadConnect) client. Only run the receiver on networks you trust.
- **Native bindings** - bugs in the native bindings that mishandle attacker-influenced input (e.g. from a spoofed packet) are in scope, including anything that could escalate beyond simple virtual-controller misbehavior.
- The receiver does not require internet access for core functionality; it only needs to be reachable on the local network from the PadConnect Android client.

## Out of Scope

- Issues that require physical access to an already compromised machine
- Social engineering attacks
- Vulnerabilities in Windows OS method ViGEmBus itself, please report those to the [ViGEm project](https://github.com/ViGEm/ViGEmBus) directly
- Vulnerabilities in third party dependencies with no practical exploit path through PadConnectReceiver itself (please also report these upstream)

## Disclosure

We ask that you give us a reasonable opportunity to investigate and fix a reported vulnerability before any public disclosure. Credit will be given in release notes for responsibly disclosed issues, unless you prefer to remain anonymous.
