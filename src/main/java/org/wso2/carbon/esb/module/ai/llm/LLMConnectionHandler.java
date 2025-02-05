/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.module.ai.llm;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.wso2.carbon.esb.module.ai.connections.ConnectionParams;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LLMConnectionHandler {

    private final static ConcurrentHashMap<String, ConnectionParams> connectionMap = new ConcurrentHashMap<>();

    public static void addConnection(String connectionName, ConnectionParams connectionParams) {
        connectionMap.computeIfAbsent(connectionName, k -> connectionParams);
    }

    public static ChatLanguageModel getChatModel(
            String connectionName, String modelName, Double temperature, Integer maxTokens,
            Double topP, Double frequencyPenalty, Integer seed) {

        ChatLanguageModel chatModel = null;
        ConnectionParams connectionParams = connectionMap.get(connectionName);
        switch (Objects.requireNonNull(connectionParams).getConnectionType()) {
            case "OPEN_AI":
                // Null values of LLM params will be handled by LangChain4j
                 chatModel =  OpenAiChatModel.builder()
                         .modelName(modelName)
                         .temperature(temperature)
                         .maxTokens(maxTokens)
                         .topP(topP)
                         .frequencyPenalty(frequencyPenalty)
                         .seed(seed)
                         .apiKey(connectionParams.getConnectionProperty("apiKey"))
                         .build();
                 break;
            case "AZURE_OPEN_AI":
                // Null values of LLM params will be handled by LangChain4j
                chatModel =  AzureOpenAiChatModel.builder()
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .topP(topP)
                        .frequencyPenalty(frequencyPenalty)
                        .seed(Long.valueOf(seed))
                        .apiKey(connectionParams.getConnectionProperty("apiKey"))
                        .deploymentName(connectionParams.getConnectionProperty("deploymentName"))
                        .endpoint(connectionParams.getConnectionProperty("endpoint"))
                        .build();
                break;
            case "ANTHROPIC":
                chatModel = AnthropicChatModel.builder()
                        .modelName(modelName)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .topP(topP)
                        .apiKey(connectionParams.getConnectionProperty("apiKey"))
                        .build();
                break;
            case "MISTRAL_AI":
                chatModel = MistralAiChatModel.builder()
                        .modelName(modelName)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .topP(topP)
                        .apiKey(connectionParams.getConnectionProperty("apiKey"))
                        .build();
                break;
            default:
                break;
        }
        return chatModel;
    }

    public static EmbeddingModel getEmbeddingModel(String connectionName, String modelName) {
        EmbeddingModel embeddingModel = null;
        ConnectionParams connectionParams = connectionMap.get(connectionName);
        switch (Objects.requireNonNull(connectionParams).getConnectionType()) {
            case "OPEN_AI":
                // Null values of LLM params will be handled by LangChain4j
                embeddingModel = OpenAiEmbeddingModel.builder()
                        .apiKey(connectionParams.getConnectionProperty("apiKey"))
                        .modelName(modelName)
                        .build();
            case "ANTHROPIC":
                // To be implemented
                break;
            default:
                break;
        }
        return embeddingModel;
    }
}
