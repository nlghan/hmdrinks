export const decodeJwt = (token) => {
    if (token) {
      const base64Payload = token.split(".")[1];
      if (base64Payload) {
        const padding = base64Payload.length % 4;
        const paddedPayload = padding === 0 ? base64Payload : base64Payload + "=".repeat(4 - padding);
        try {
          const decodedPayload = decodeURIComponent(
            atob(paddedPayload)
              .split("")
              .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
              .join("")
          );
          return JSON.parse(decodedPayload);
        } catch (error) {
          console.error("Error decoding JWT payload", error);
        }
      }
    }
    return undefined;
  };
  