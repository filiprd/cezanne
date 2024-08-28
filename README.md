Cezanne is a [ZIO](https://zio.dev/) demo project which implements several backend verticals for an online paintings retailer platform. The domain is based on a sister project, [monet](https://github.com/filiprd/monet), which is a full backend implementation with the [Typelevel](https://typelevel.org/) stack.

The purpose of this project is to demonstrate how ZIO can be used to implement backend APIs. Unlike [monet](https://github.com/filiprd/monet) however, cezanne does not aim to provide full implementation of the domain, and therefore does not fully cover all use cases or tech-related topics such as test coverage, property-based testing, http requests validation, caching, security, CI pipeline, etc.

Tech stack:
- [ZIO](https://zio.dev/)
- [Tapir](https://github.com/softwaremill/tapir)
- [Quill](https://zio.dev/zio-quill/)
- [Testcontainers](https://testcontainers.com/)