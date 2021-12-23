# hadolint ignore=DL3006
FROM busybox AS builder

CMD [ "ps", "faux" ]
